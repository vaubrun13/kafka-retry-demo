package com.acme.kafkaretrydemo.consumers;

import avro.generated.demo.EventKey;
import avro.generated.demo.EventValue;
import com.acme.kafkaretrydemo.exceptions.BusinessException;
import com.acme.kafkaretrydemo.models.RequestObject;
import com.acme.kafkaretrydemo.services.BusinessService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

/**
 * This consumer is used to showcase non-blocking retries with Kafka.
 */
@Component
public class OtherKafkaConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(OtherKafkaConsumer.class);

  private final BusinessService businessService;

  public final Map<UUID, List<EventValue>> deadMessages = new ConcurrentHashMap<>();

  public OtherKafkaConsumer(BusinessService businessService) {
    this.businessService = businessService;
  }

  @RetryableTopic(
      attempts = "3",
      autoCreateTopics = "true",
      dltStrategy = DltStrategy.FAIL_ON_ERROR, //To avoid infinite retries in DLT
      dltTopicSuffix = ".DLT",
      backoff = @Backoff(delay = 200, maxDelay = 10000, multiplier = 2))
  @KafkaListener(topics = "my-demo-topic-async-retries")
  public void handleMessage(
      @Payload ConsumerRecord<EventKey, EventValue> record, Acknowledgment ack) {

    LOGGER.info("Received message for user with id: {}", record.key().getId());

    try {
      // Map avro object to request object
      RequestObject requestObject = EventMapper.mapToRequestObject(record.value());
      // Call business service to perform some operation
      this.businessService.updateSomething(requestObject);
    } catch (BusinessException e) {
      LOGGER.info(
          "Business exception occurred while processing message for entity: {}",
          record.key().getId());
    } finally {
      ack.acknowledge();
    }
  }

  // This is to test the DLT functionality
  @KafkaListener(topics = "my-demo-topic-async-retries.DLT")
  public void handleDltMessage(
      @Payload ConsumerRecord<EventKey, EventValue> record, Acknowledgment ack) {
    UUID id = UUID.fromString(record.key().getId());
    LOGGER.info("Received message for user with id: {} from DLT", id);
    deadMessages.computeIfAbsent(id, k -> new ArrayList<>()).add(record.value());

    ack.acknowledge();
  }
}
