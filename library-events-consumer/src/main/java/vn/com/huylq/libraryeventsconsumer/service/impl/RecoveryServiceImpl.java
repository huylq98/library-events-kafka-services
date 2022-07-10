package vn.com.huylq.libraryeventsconsumer.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;
import vn.com.huylq.libraryeventsconsumer.constant.FailureRecordStatus;
import vn.com.huylq.libraryeventsconsumer.entity.FailureRecord;
import vn.com.huylq.libraryeventsconsumer.repository.FailureRecordRepository;
import vn.com.huylq.libraryeventsconsumer.service.RecoveryService;

@Service
@Slf4j
public class RecoveryServiceImpl implements RecoveryService {

  private final FailureRecordRepository failureRecordRepository;

  public RecoveryServiceImpl(FailureRecordRepository failureRecordRepository) {
    this.failureRecordRepository = failureRecordRepository;
  }

  @Override
  public void handleFailureRecord(ConsumerRecord<Integer, String> consumerRecord, Exception e,
      FailureRecordStatus status) {
    log.info("Failure Record: {}, exception: {}, status: {}", consumerRecord,
        e.getCause().getCause(),
        status);
    FailureRecord failureRecord = FailureRecord.builder()
        .topic(consumerRecord.topic())
        .partition(consumerRecord.partition())
        .key(consumerRecord.key())
        .errorRecord(consumerRecord.value())
        .offset(consumerRecord.offset())
        .status(status)
        .timestamp(consumerRecord.timestamp())
        .build();
    failureRecordRepository.save(failureRecord);
  }
}
