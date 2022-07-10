package vn.com.huylq.libraryeventsconsumer.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.com.huylq.libraryeventsconsumer.constant.FailureRecordStatus;
import vn.com.huylq.libraryeventsconsumer.entity.FailureRecord;
import vn.com.huylq.libraryeventsconsumer.repository.FailureRecordRepository;
import vn.com.huylq.libraryeventsconsumer.service.LibraryEventsService;

@Component
@Slf4j
@RequiredArgsConstructor
public class RetryScheduler {

  private final FailureRecordRepository failureRecordRepository;
  private final LibraryEventsService libraryEventsService;

  @Scheduled(fixedDelay = 10_000)
  public void retryFailureRecords() {
    log.info("Retry Failure Records Scheduler started!!!");
    failureRecordRepository.findAllByStatus(FailureRecordStatus.RETRY)
        .forEach(failureRecord -> {
          try {
            log.info("Retry Scheduler for failure record: {}", failureRecord);
            ConsumerRecord<Integer, String> consumerRecord = buildConsumerRecord(failureRecord);
            libraryEventsService.process(consumerRecord);
            failureRecord.setStatus(FailureRecordStatus.SUCCESS);
            failureRecordRepository.save(failureRecord);
          } catch (Exception e) {
            log.error("Retry Scheduler exception: {}", e.getCause().getMessage());
          }
        });
    log.info("Retry Failure Records Scheduler finished!!!");
  }

  private ConsumerRecord<Integer, String> buildConsumerRecord(FailureRecord failureRecord) {
    return new ConsumerRecord<>(
        failureRecord.getTopic(),
        failureRecord.getPartition(),
        failureRecord.getOffset(),
        failureRecord.getKey(),
        failureRecord.getErrorRecord()
    );
  }

}
