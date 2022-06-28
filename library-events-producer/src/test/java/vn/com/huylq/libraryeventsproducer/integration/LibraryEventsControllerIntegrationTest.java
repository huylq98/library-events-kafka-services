package vn.com.huylq.libraryeventsproducer.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;
import vn.com.huylq.libraryeventsproducer.domain.Book;
import vn.com.huylq.libraryeventsproducer.domain.LibraryEvent;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = {"library-events"}, partitions = 3)
@TestPropertySource(properties = {
    "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}"
})
class LibraryEventsControllerIntegrationTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private EmbeddedKafkaBroker embeddedKafkaBroker;

  private Consumer<Integer, String> consumer;

  @BeforeEach
  void setup() {
    Map<String, Object> configs = new HashMap<>(
        KafkaTestUtils.consumerProps("group-1", "true", embeddedKafkaBroker));
    consumer = new DefaultKafkaConsumerFactory<>(
        configs, new IntegerDeserializer(), new StringDeserializer()
    ).createConsumer();
    embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);
  }

  @AfterEach
  void tearDown() {
    consumer.close();
  }

  @Test
  @Timeout(3)
  void scanNewBookTest_withTestRestTemplate() {
    // given
    Book book = Book.builder()
        .id(1)
        .name("Black Phone")
        .author("Stephen")
        .build();

    LibraryEvent libraryEvent = LibraryEvent.builder()
        .id(null)
        .book(book)
        .build();

    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
    HttpEntity<LibraryEvent> httpEntity = new HttpEntity<>(libraryEvent, headers);

    // when
    ResponseEntity<LibraryEvent> response = restTemplate.exchange("/v1/library-event",
        HttpMethod.POST, httpEntity, LibraryEvent.class);

    // then
    assertEquals(HttpStatus.CREATED, response.getStatusCode());

    ConsumerRecord<Integer, String> consumerRecord = KafkaTestUtils.getSingleRecord(consumer,
        "library-events");
    String expectedValue = "{\"id\":null,\"libraryEventType\":\"NEW\",\"book\":{\"id\":1,\"name\":\"Black Phone\",\"author\":\"Stephen\"}}";
    assertEquals(expectedValue, consumerRecord.value());
  }
}
