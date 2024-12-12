package com.acme.kafkaretrydemo.configuration;

import com.acme.kafkaretrydemo.exceptions.MyFatalException;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.retrytopic.RetryTopicConfigurationSupport;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling // Needed for async retries
public class AsyncRetryConfiguration extends RetryTopicConfigurationSupport {

  @Override
  protected void manageNonBlockingFatalExceptions(
      List<Class<? extends Throwable>> nonBlockingFatalExceptions) {

    nonBlockingFatalExceptions.add(NullPointerException.class);
    nonBlockingFatalExceptions.add(EnumConstantNotPresentException.class);
    nonBlockingFatalExceptions.add(MyFatalException.class);
  }

  @Override
  protected void configureCustomizers(
      CustomizersConfigurer customizersConfigurer) {
    customizersConfigurer.customizeDeadLetterPublishingRecoverer(
        dlpr -> {
          dlpr.setAppendOriginalHeaders(true); //To keep original headers
          dlpr.setStripPreviousExceptionHeaders(false); //To stack errors
        });
  }
}
