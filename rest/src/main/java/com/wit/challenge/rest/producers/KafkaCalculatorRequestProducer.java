package com.wit.challenge.rest.producers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class KafkaCalculatorRequestProducer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaCalculatorRequestProducer.class);

    @Autowired
    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaCalculatorRequestProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public String send(String operation, String a, String b) {
        String requestId = UUID.randomUUID().toString();
        String message = operation + "," + a + "," + b + "," + requestId;

        logger.info("Sending message to topic 'calculator.request' with RequestId: {}. Content: {}", requestId, message);

        kafkaTemplate.send("calculator.request", message);
        return requestId;
    }
}