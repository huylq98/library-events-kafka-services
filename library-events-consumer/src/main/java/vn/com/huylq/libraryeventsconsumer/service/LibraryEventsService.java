package vn.com.huylq.libraryeventsconsumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface LibraryEventsService {

  void process(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException;
}
