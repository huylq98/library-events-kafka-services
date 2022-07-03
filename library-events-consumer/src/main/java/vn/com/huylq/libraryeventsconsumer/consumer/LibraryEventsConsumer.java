package vn.com.huylq.libraryeventsconsumer.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import vn.com.huylq.libraryeventsconsumer.service.LibraryEventsService;

@Component
@RequiredArgsConstructor
@Slf4j
public class LibraryEventsConsumer {

  private final LibraryEventsService libraryEventsService;

  @KafkaListener(topics = {"library-events"})
  public void onMessage(ConsumerRecord<Integer, String> consumerRecord)
      throws JsonProcessingException {
    log.info("ConsumerRecord: {}", consumerRecord);
    libraryEventsService.process(consumerRecord);
  }
}
