package com.wit.challenge.rest.producers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KafkaCalculatorRequestProducerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private KafkaCalculatorRequestProducer producer;

    @Test
    void testSendGeneratesCorrectMessageAndSendsToKafka() {
        String operation = "sum";
        String a = "10.0";
        String b = "20.0";

        producer.send(operation, a, b);

        verify(kafkaTemplate, times(1)).send(eq("calculator.request"), startsWith("sum,10.0,20.0,"));
    }
}