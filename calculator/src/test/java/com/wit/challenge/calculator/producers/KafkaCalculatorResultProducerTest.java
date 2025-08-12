package com.wit.challenge.calculator.producers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class KafkaCalculatorResultProducerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private KafkaCalculatorResultProducer producer;

    @Test
    void testSendForSuccess() {
        String requestId = "test-uuid-123";
        String status = "OK";
        String payload = "15.0";
        String expectedMessage = "test-uuid-123,OK,15.0";

        producer.send(requestId, status, payload);

        verify(kafkaTemplate, times(1)).send(eq("calculator.result"), eq(expectedMessage));
    }

    @Test
    void testSendForError() {
        String requestId = "test-uuid-456";
        String status = "ERROR";
        String payload = "Division by zero is not allowed.";
        String expectedMessage = "test-uuid-456,ERROR,Division by zero is not allowed.";

        producer.send(requestId, status, payload);

        verify(kafkaTemplate, times(1)).send(eq("calculator.result"), eq(expectedMessage));
    }
}