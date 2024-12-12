package com.acme.kafkaretrydemo;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.acme.kafkaretrydemo.consumers.OneKafkaConsumer;
import com.acme.kafkaretrydemo.exceptions.BusinessException;
import com.acme.kafkaretrydemo.exceptions.MyFatalException;
import com.acme.kafkaretrydemo.models.RequestObject;
import com.acme.kafkaretrydemo.services.BusinessService;
import com.acme.kafkaretrydemo.utils.FakePublisher;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class OneKafkaConsumerTest {

  private static final String TOPIC = "my-demo-topic";

  @Container
  static KafkaContainer kafkaContainer =
      new KafkaContainer(
          DockerImageName.parse("apache/kafka-native:3.8.0")
              .asCompatibleSubstituteFor("apache/kafka"));

  @DynamicPropertySource
  static void kafka(DynamicPropertyRegistry registry) {
    registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
  }

  @Autowired private FakePublisher fakePublisher;
  @Autowired private OneKafkaConsumer consumer;
  @MockBean private BusinessService businessService;

  @Test
  void given_fatal_should_not_retry() {
    // Given
    UUID id = UUID.randomUUID();
    String firstName = "Johnny";
    String lastName = "Goode";
    RequestObject expectedRequestObject = new RequestObject(id, firstName, lastName);
    doThrow(MyFatalException.class).when(businessService).updateSomething(expectedRequestObject);
    // When
    fakePublisher.publishForCustomer(id, firstName, lastName, TOPIC);
    // Then
    await()
        .atMost(Duration.ofSeconds(10))
        .untilAsserted(
            () -> {
              assertAll(
                  () -> assertThat(consumer.deadMessages).containsKey(id),
                  () ->
                      verify(businessService, times(1)).updateSomething(eq(expectedRequestObject)));
            });
  }

  @Test
  void given_non_fatal_should_retry() {
    // Given
    UUID id = UUID.randomUUID();
    String firstName = "John";
    String lastName = "Doe";
    RequestObject expectedRequestObject = new RequestObject(id, firstName, lastName);
    doThrow(RuntimeException.class).when(businessService).updateSomething(expectedRequestObject);
    // When
    fakePublisher.publishForCustomer(id, firstName, lastName,TOPIC);
    // Then
    await()
        .atMost(Duration.ofSeconds(3))
        .untilAsserted(
            () -> {
              assertAll(
                  () -> assertThat(consumer.deadMessages).containsKey(id),
                  () -> verify(businessService, times(4)).updateSomething(expectedRequestObject));
            });

  }

  @Test
  void given_business_exception_should_ack() {
    // Given
    UUID id = UUID.randomUUID();
    String firstName = "John";
    String lastName = "Doe";
    RequestObject expectedRequestObject = new RequestObject(id, firstName, lastName);
    doThrow(BusinessException.class).when(businessService).updateSomething(expectedRequestObject);
    // When
    fakePublisher.publishForCustomer(id, firstName, lastName, TOPIC);
    // Then
    await()
        .atMost(Duration.ofSeconds(3))
        .untilAsserted(
            () -> {
              verify(businessService, times(1)).updateSomething(expectedRequestObject);
            });

    await()
        .during(Duration.ofSeconds(3))
        .untilAsserted(
            () -> {
              assertThat(consumer.deadMessages).doesNotContainKey(id);
            });
  }
}
