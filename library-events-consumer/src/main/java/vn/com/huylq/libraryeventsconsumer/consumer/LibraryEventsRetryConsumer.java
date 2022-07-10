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
public class LibraryEventsRetryConsumer {

  private final LibraryEventsService libraryEventsService;

  @KafkaListener(topics = {"${topics.retry}"},
      autoStartup = "${retryListener.autoStartup:true}",
      groupId = "retry-consumer-group")
  public void onMessage(ConsumerRecord<Integer, String> consumerRecord)
      throws JsonProcessingException {
    log.info("RetryConsumerRecord: {}", consumerRecord);
    libraryEventsService.process(consumerRecord);
  }
}
