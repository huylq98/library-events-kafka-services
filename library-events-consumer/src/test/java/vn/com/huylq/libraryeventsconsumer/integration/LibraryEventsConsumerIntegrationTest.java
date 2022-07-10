package vn.com.huylq.libraryeventsconsumer.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.TestPropertySource;
import vn.com.huylq.libraryeventsconsumer.constant.LibraryEventType;
import vn.com.huylq.libraryeventsconsumer.consumer.LibraryEventsConsumer;
import vn.com.huylq.libraryeventsconsumer.entity.Book;
import vn.com.huylq.libraryeventsconsumer.entity.LibraryEvent;
import vn.com.huylq.libraryeventsconsumer.repository.FailureRecordRepository;
import vn.com.huylq.libraryeventsconsumer.repository.LibraryEventsRepository;
import vn.com.huylq.libraryeventsconsumer.service.LibraryEventsService;

@SpringBootTest
@EmbeddedKafka(topics = {"library-events", "library-events.RETRY",
    "library-events.DLT"}, partitions = 3)
@TestPropertySource(properties = {
    "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "retryListener.autoStartup=false"
})
@SuppressWarnings("all")
public class LibraryEventsConsumerIntegrationTest {

  @Autowired
  EmbeddedKafkaBroker embeddedKafkaBroker;

  @Autowired
  KafkaTemplate<Integer, String> kafkaTemplate;

  @Autowired
  KafkaListenerEndpointRegistry endpointRegistry;

  @SpyBean
  LibraryEventsConsumer libraryEventsConsumer;

  @SpyBean
  LibraryEventsService libraryEventsService;

  @Autowired
  LibraryEventsRepository libraryEventsRepository;

  @Autowired
  ObjectMapper objectMapper;

  Consumer<Integer, String> consumer;

  @Value("${topics.retry}")
  String retryTopic;

  @Value("${topics.dlt}")
  String deadLetterTopic;

  @Autowired
  FailureRecordRepository failureRecordRepository;

  @BeforeEach
  void setUp() {
    endpointRegistry.getListenerContainers()
        .stream()
        .filter(messageListenerContainer -> "library-events-listener-group".equals(
            messageListenerContainer.getGroupId()))
        .forEach(messageListenerContainer ->
            ContainerTestUtils.waitForAssignment(
                messageListenerContainer,
                embeddedKafkaBroker.getPartitionsPerTopic()));
  }

  @AfterEach
  void tearDown() {
    libraryEventsRepository.deleteAll();
    failureRecordRepository.deleteAll();
  }

  @Test
  void publishNewLibraryEvent()
      throws ExecutionException, InterruptedException, JsonProcessingException {
    // given
    String libraryEvent = "{\"libraryEventType\":\"NEW\",\"book\":{\"id\":1,\"name\":\"The Black Phone\",\"author\":\"Joe Hill\"}}";
    kafkaTemplate.sendDefault(libraryEvent).get();

    // when
    CountDownLatch countDownLatch = new CountDownLatch(1);
    countDownLatch.await(3, TimeUnit.SECONDS);

    // then
    verify(libraryEventsConsumer, times(1)).onMessage(any(ConsumerRecord.class));
    verify(libraryEventsService, times(1)).process(any(ConsumerRecord.class));

    List<LibraryEvent> libraryEvents = libraryEventsRepository.findAll();
    assertEquals(1, libraryEvents.size());
    assertNotNull(libraryEvents.get(0).getBook());
    assertEquals(1, libraryEvents.get(0).getBook().getId());
  }

  @Test
  void updateExistedLibraryEvent()
      throws InterruptedException, JsonProcessingException, ExecutionException {
    // given
    Book book = Book.builder()
        .id(1)
        .name("Invalid Name")
        .author("Joe Hill")
        .build();

    LibraryEvent libraryEvent = LibraryEvent.builder()
        .libraryEventType(LibraryEventType.NEW)
        .book(book)
        .build();

    libraryEventsRepository.save(libraryEvent);

    book.setName("The Black Phone");
    libraryEvent.setLibraryEventType(LibraryEventType.UPDATE);
    kafkaTemplate.sendDefault(objectMapper.writeValueAsString(libraryEvent)).get();

    // when
    CountDownLatch countDownLatch = new CountDownLatch(1);
    countDownLatch.await(3, TimeUnit.SECONDS);

    // then
    verify(libraryEventsConsumer, times(1)).onMessage(any(ConsumerRecord.class));
    verify(libraryEventsService, times(1)).process(any(ConsumerRecord.class));

    List<LibraryEvent> libraryEvents = libraryEventsRepository.findAll();
    assertEquals(1, libraryEvents.size());
    assertNotNull(libraryEvents.get(0).getBook());
    assertEquals("The Black Phone", libraryEvents.get(0).getBook().getName());
  }

  @Test
  void updateExistedLibraryEvent_withNullLibraryEventId()
      throws InterruptedException, JsonProcessingException, ExecutionException {
    // given
    String libraryEvent = "{\"libraryEventType\":\"UPDATE\",\"book\":{\"id\":1,\"name\":\"The Black Phone\",\"author\":\"Joe Hill\"}}";
    kafkaTemplate.sendDefault(libraryEvent).get();

    // when
    CountDownLatch countDownLatch = new CountDownLatch(1);
    countDownLatch.await(5, TimeUnit.SECONDS);

    // then
    verify(libraryEventsConsumer, times(1)).onMessage(any(ConsumerRecord.class));
    verify(libraryEventsService, times(1)).process(any(ConsumerRecord.class));

    /**
     * topicRecovery
     *     Map<String, Object> configs = new HashMap<>(
     *         KafkaTestUtils.consumerProps("null-id-listener-group", "true", embeddedKafkaBroker));
     *     consumer = new DefaultKafkaConsumerFactory<>(
     *         configs, new IntegerDeserializer(), new StringDeserializer()
     *     ).createConsumer();
     *     embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, deadLetterTopic);
     *
     *     ConsumerRecord<Integer, String> consumerRecord = KafkaTestUtils.getSingleRecord(consumer,
     *         deadLetterTopic);
     *
     *     assertEquals(libraryEvent, consumerRecord.value());
     */

    assertEquals(1, failureRecordRepository.count());
    failureRecordRepository.findAll()
        .forEach(failureRecord -> System.out.println("failureRecord: " + failureRecord));
  }

  @Test
  void updateExistedLibraryEvent_withInvalidJson()
      throws InterruptedException, JsonProcessingException, ExecutionException {
    // given
    String libraryEvent = "{\"libraryEventType:\"UPDATE\",\"book\":{\"id\":1,\"name\":\"The Black Phone\",\"author\":\"Joe Hill\"}}";
    kafkaTemplate.sendDefault(libraryEvent).get();

    // when
    CountDownLatch countDownLatch = new CountDownLatch(1);
    countDownLatch.await(5, TimeUnit.SECONDS);

    // then
    verify(libraryEventsConsumer, times(3)).onMessage(any(ConsumerRecord.class));
    verify(libraryEventsService, times(3)).process(any(ConsumerRecord.class));

    /**
     * topicRecovery
     * Map<String, Object> configs = new HashMap<>(
     *         KafkaTestUtils.consumerProps("invalid-json-listener-group", "true", embeddedKafkaBroker));
     *     consumer = new DefaultKafkaConsumerFactory<>(
     *         configs, new IntegerDeserializer(), new StringDeserializer()
     *     ).createConsumer();
     *     embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, retryTopic);
     *
     *     ConsumerRecord<Integer, String> consumerRecord = KafkaTestUtils.getSingleRecord(consumer,
     *         retryTopic);
     *
     *     assertEquals(libraryEvent, consumerRecord.value());
     */

    assertEquals(1, failureRecordRepository.count());
    failureRecordRepository.findAll()
        .forEach(failureRecord -> System.out.println("failureRecord: " + failureRecord));
  }
}
