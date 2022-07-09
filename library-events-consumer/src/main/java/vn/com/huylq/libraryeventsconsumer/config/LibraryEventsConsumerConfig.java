package vn.com.huylq.libraryeventsconsumer.config;

import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

@Configuration
@EnableKafka
@Slf4j
@SuppressWarnings("java:S1452")
public class LibraryEventsConsumerConfig {

  public CommonErrorHandler errorHandler() {
    List<Class<IllegalArgumentException>> notRetryableExceptions = Collections.singletonList(IllegalArgumentException.class);
    ExponentialBackOff exponentialBackOff = new ExponentialBackOff(1_000L, 2.0);
    DefaultErrorHandler errorHandler = new DefaultErrorHandler(exponentialBackOff);
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
