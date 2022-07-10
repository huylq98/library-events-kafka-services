package vn.com.huylq.libraryeventsconsumer.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import vn.com.huylq.libraryeventsconsumer.constant.FailureRecordStatus;

public interface RecoveryService {

  void handleFailureRecord(ConsumerRecord<Integer, String> consumerRecord, Exception e,
      FailureRecordStatus status);
}
