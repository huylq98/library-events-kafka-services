package vn.com.huylq.libraryeventsconsumer.config;

import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;
import vn.com.huylq.libraryeventsconsumer.constant.FailureRecordStatus;
import vn.com.huylq.libraryeventsconsumer.service.RecoveryService;

@Configuration
@EnableKafka
@Slf4j
@SuppressWarnings("all")
public class LibraryEventsConsumerConfig {

  private final KafkaTemplate kafkaTemplate;
  private final String retryTopic;
  private final String deadLetterTopic;

  @Autowired
  private RecoveryService recoveryService;

  public LibraryEventsConsumerConfig(KafkaTemplate kafkaTemplate,
      @Value("${topics.retry}") String retryTopic,
      @Value("${topics.dlt}") String deadLetterTopic) {
    this.kafkaTemplate = kafkaTemplate;
    this.retryTopic = retryTopic;
    this.deadLetterTopic = deadLetterTopic;
  }

  public ConsumerRecordRecoverer topicRecovery() {
    return new DeadLetterPublishingRecoverer(kafkaTemplate,
        (r, e) -> {
          if (e.getCause() instanceof IllegalArgumentException) {
            log.info("Publish dead-letter record to {} topic", deadLetterTopic);
            return new TopicPartition(deadLetterTopic, r.partition());
          } else {
            log.info("Publish dead-letter record to {} topic", retryTopic);
            return new TopicPartition(retryTopic, r.partition());
          }
        });
  }

  public ConsumerRecordRecoverer databaseRecovery() {
    return new ConsumerRecordRecoverer() {
      @Override
      public void accept(ConsumerRecord<?, ?> consumerRecord, Exception e) {
        ConsumerRecord<Integer, String> failureRecord = (ConsumerRecord<Integer, String>) consumerRecord;
        if (e.getCause() instanceof IllegalArgumentException) {
          log.info("Dead-letter record status: {}", FailureRecordStatus.DEAD);
          recoveryService.handleFailureRecord(failureRecord, e, FailureRecordStatus.DEAD);
        } else {
          log.info("Dead-letter record status: {}", FailureRecordStatus.RETRY);
          recoveryService.handleFailureRecord(failureRecord, e, FailureRecordStatus.RETRY);
        }
      }
    };
  }

  public CommonErrorHandler errorHandler() {
    List<Class<IllegalArgumentException>> notRetryableExceptions = Collections.singletonList(
        IllegalArgumentException.class);
    ExponentialBackOff exponentialBackOff = new ExponentialBackOff(1_000L, 2.0);
    exponentialBackOff.setMaxElapsedTime(2_000L);
    DefaultErrorHandler errorHandler = new DefaultErrorHandler(databaseRecovery(),
        exponentialBackOff);
    errorHandler.setRetryListeners((kafkaRecord, ex, deliveryAttempt) -> log.error(
        "Failed in Record Listener, exception: {}, deliveryAttempt: {}", ex.getMessage(),
        deliveryAttempt));
    notRetryableExceptions.forEach(errorHandler::addNotRetryableExceptions);
    return errorHandler;
  }

  @Bean
  ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
      ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
      ObjectProvider<ConsumerFactory<Object, Object>> kafkaConsumerFactory) {
    ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
    configurer.configure(factory, kafkaConsumerFactory.getIfAvailable());
    factory.setConcurrency(3);
    factory.setCommonErrorHandler(errorHandler());
    return factory;
  }
}
