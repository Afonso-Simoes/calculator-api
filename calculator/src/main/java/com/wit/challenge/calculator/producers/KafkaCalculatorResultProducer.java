package com.wit.challenge.calculator.producers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaCalculatorResultProducer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaCalculatorResultProducer.class);

    @Autowired
    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaCalculatorResultProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(String requestId, String status, String payload) {
        String responseMessage = String.format("%s,%s,%s", requestId, status, payload);

        logger.info("Sending response to topic 'calculator.result' with RequestId: {}. Status: {}, Payload: {}", requestId, status, payload);

        kafkaTemplate.send("calculator.result", responseMessage);
    }
}