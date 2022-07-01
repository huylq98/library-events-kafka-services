package vn.com.huylq.libraryeventsproducer.unit.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;
import vn.com.huylq.libraryeventsproducer.domain.Book;
import vn.com.huylq.libraryeventsproducer.domain.LibraryEvent;
import vn.com.huylq.libraryeventsproducer.producer.LibraryEventsProducer;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("java:S5783")
class LibraryEventsProducerUnitTest {


    @Mock
    KafkaTemplate<Integer, String> kafkaTemplate;

    @Mock
    Environment environment;

    @Spy
    ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    LibraryEventsProducer libraryEventsProducer;

    @Test
    void sendLibraryEvent_failure() {
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

        SettableListenableFuture<SendResult<Integer, String>> future = new SettableListenableFuture<>();
        future.setException(new RuntimeException("Exception Calling Kafka"));

        // when
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);

        // then
        assertThrows(Exception.class, () -> libraryEventsProducer.sendLibraryEvents(libraryEvent).get());
    }

    @Test
    void sendLibraryEvent_success() throws JsonProcessingException, ExecutionException, InterruptedException {
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

        SettableListenableFuture<SendResult<Integer, String>> future = new SettableListenableFuture<>();

        String recordValue = objectMapper.writeValueAsString(libraryEvent);
        String topic = "library-events";
        ProducerRecord<Integer, String> producerRecord = new ProducerRecord<>(topic, libraryEvent.getId(), recordValue);
        RecordMetadata recordMetadata = new RecordMetadata(new TopicPartition(topic, 1), 1, 1, 1, 1, 1);
        SendResult<Integer, String> sendResult = new SendResult<>(producerRecord, recordMetadata);
        future.set(sendResult);

        // when
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);
        ListenableFuture<SendResult<Integer, String>> listenableFuture = libraryEventsProducer.sendLibraryEvents(
                libraryEvent);

        // then
        SendResult<Integer, String> result = listenableFuture.get();
        assertEquals(1, result.getRecordMetadata().partition());

    }

}
