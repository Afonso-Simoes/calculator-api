package com.wit.challenge.rest.consumers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.wit.challenge.rest.dtos.CalculationDTO;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class KafkaCalculatorResultConsumer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaCalculatorResultConsumer.class);

    private final ConcurrentHashMap<String, CompletableFuture<CalculationDTO>> pendingRequests = new ConcurrentHashMap<>();

    public void registerRequest(String requestId, CompletableFuture<CalculationDTO> future) {
        pendingRequests.put(requestId, future);
    }

    @KafkaListener(topics = "calculator.result", groupId = "result-group")
    public void consume(String message) {
        logger.info("Result message received from Kafka: {}.", message);
        String[] parts = message.split(",");

        if (parts.length != 3) {
            logger.error("Result message has an invalid format: {}. Ignoring.", message);
            return;
        }

        String requestId = parts[0];
        String status = parts[1];
        String payload = parts[2]; 

        CompletableFuture<CalculationDTO> future = pendingRequests.remove(requestId);

        if (future != null) {
            logger.info("Matched request with RequestId: {}. Response: Status='{}', Payload='{}'.", requestId, status, payload);
            CalculationDTO resultDTO = new CalculationDTO(status, payload);
            future.complete(resultDTO);
        } else {
            logger.warn("Result message received for RequestId '{}' that is not pending. Ignoring.", requestId);
        }
    }
}