package com.acme.kafkaretrydemo.consumers;

import avro.generated.demo.EventValue;
import com.acme.kafkaretrydemo.models.RequestObject;
import java.util.UUID;

public class EventMapper {

  public static RequestObject mapToRequestObject(EventValue eventValue) {
    return new RequestObject(
        UUID.fromString(eventValue.getId()), eventValue.getFirstName(), eventValue.getLastName());
  }
}
