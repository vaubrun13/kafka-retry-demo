package com.acme.kafkaretrydemo.utils;

import avro.generated.demo.EventKey;
import java.util.UUID;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class FakePublisher {

  private final KafkaTemplate<SpecificRecord, SpecificRecord> template;

  public FakePublisher(final KafkaTemplate<SpecificRecord, SpecificRecord> template) {
    this.template = template;
  }

  public void publishForCustomer(UUID id, String firstName, String lastName, String topic) {
    EventKey key = new EventKey(id.toString());
    avro.generated.demo.EventValue value =
        new avro.generated.demo.EventValue(id.toString(), firstName, lastName);

    template.send(topic, key, value);
  }
}
