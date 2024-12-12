package com.acme.kafkaretrydemo.configuration;

import com.acme.kafkaretrydemo.exceptions.MyFatalException;
import java.util.List;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.DefaultSslBundleRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.retrytopic.RetryTopicConfigurationBuilder;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfiguration {



  @Bean
  public DefaultErrorHandler errorHandler(KafkaTemplate<SpecificRecord, SpecificRecord> template) {

    List<Class<? extends Exception>> fatalExceptions =
        List.of(
            EnumConstantNotPresentException.class,
            NullPointerException.class,
            MyFatalException.class);
    DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template);

    DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, new FixedBackOff(500, 3));

    // ERROR by default
    handler.setLogLevel(KafkaException.Level.WARN);
    // Do not retry fatal exceptions
    handler.addNotRetryableExceptions(fatalExceptions.toArray(new Class[0]));
    return handler;
  }
}
