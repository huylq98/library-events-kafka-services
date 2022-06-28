package vn.com.huylq.libraryeventsproducer.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import vn.com.huylq.libraryeventsproducer.domain.LibraryEvent;

@Component
@Slf4j
@RequiredArgsConstructor
public class LibraryEventsProducer {

  private final KafkaTemplate<Integer, String> kafkaTemplate;
  private final ObjectMapper objectMapper;
  private final Environment environment;
  private static final String TOPIC = "library-events";

  public void sendLibraryEvents(LibraryEvent libraryEvent) throws JsonProcessingException {
    Integer key = libraryEvent.getId();
    String value = objectMapper.writeValueAsString(libraryEvent);
    ProducerRecord<Integer, String> producerRecord = buildRecord(key, value);
    ListenableFuture<SendResult<Integer, String>> future = kafkaTemplate.send(producerRecord);
    future.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {
      @Override
      public void onFailure(@NonNull Throwable e) {
        handleFailure(key, value, e);
      }

      @SneakyThrows
      @Override
      public void onSuccess(SendResult<Integer, String> result) {
        handleSuccess(key, value, result);
      }
    });
  }

  private ProducerRecord<Integer, String> buildRecord(Integer k, String v) {
    List<Header> headers = Arrays.asList(
        new RecordHeader("env", Arrays.toString(environment.getActiveProfiles()).getBytes())
    );
    return new ProducerRecord<>(TOPIC, null, k, v, headers);
  }

  private void handleFailure(Integer key, String value, Throwable ex) {
    log.error("Failed to send kafka event with key: {}, value: {}, reason: {}", key, value,
        ex.getMessage());
  }

  private void handleSuccess(Integer key, String value, SendResult<Integer, String> result)
      throws JsonProcessingException {
    log.info("Send event successfully with key: {}, value: {}", key, value);
    log.info("{}", objectMapper.writeValueAsString(result.getRecordMetadata()));
  }
}
