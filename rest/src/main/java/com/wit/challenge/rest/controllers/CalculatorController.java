package com.wit.challenge.rest.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.wit.challenge.rest.producers.KafkaCalculatorRequestProducer;
import com.wit.challenge.rest.consumers.KafkaCalculatorResultConsumer;
import com.wit.challenge.rest.dtos.CalculationDTO;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@RestController
public class CalculatorController {

    private static final Logger logger = LoggerFactory.getLogger(CalculatorController.class);

    private final KafkaCalculatorRequestProducer kafkaCalculatorRequestProducer;
    private final KafkaCalculatorResultConsumer kafkaCalculatorResultConsumer;

    public CalculatorController(KafkaCalculatorRequestProducer kafkaCalculatorRequestProducer, KafkaCalculatorResultConsumer kafkaCalculatorResultConsumer) {
        this.kafkaCalculatorRequestProducer = kafkaCalculatorRequestProducer;
        this.kafkaCalculatorResultConsumer = kafkaCalculatorResultConsumer;
    }

    @GetMapping("/{operation}")
    public CompletableFuture<ResponseEntity<CalculationDTO>> calculate(
            @PathVariable String operation,
            @RequestParam String a,
            @RequestParam String b) {

        logger.info("HTTP request received: operation='{}', a='{}', b='{}'.", operation, a, b);

        if (!("sum".equals(operation) || "subtract".equals(operation) || "multiply".equals(operation) || "divide".equals(operation))) {
            logger.warn("Validation failed: invalid operation '{}'.", operation);
            return CompletableFuture.completedFuture(
                ResponseEntity.badRequest().body(new CalculationDTO("ERROR", "Invalid operation. Valid operations are: sum, subtract, multiply, divide."))
            );
        }

        try {
            Double.parseDouble(a);
            Double.parseDouble(b);
        } catch (NumberFormatException e) {
            logger.warn("Validation failed: parameters are not numbers. a='{}', b='{}'.", a, b);
            return CompletableFuture.completedFuture(
                ResponseEntity.badRequest().body(new CalculationDTO("ERROR", "Parameters 'a' and 'b' must be valid numbers."))
            );
        }

        CompletableFuture<CalculationDTO> futureResult = new CompletableFuture<>();
        String requestId = kafkaCalculatorRequestProducer.send(operation, a, b);

        MDC.put("requestId", requestId);
        logger.info("Calculation request sent to Kafka with RequestId: {}.", requestId);

        kafkaCalculatorResultConsumer.registerRequest(requestId, futureResult);
        
        return futureResult.orTimeout(10, TimeUnit.SECONDS)
            .thenApply(result -> {
                MDC.put("requestId", requestId);
                logger.info("Response received from Kafka for RequestId {}.", requestId);
                
                try {
                    if ("ERROR".equals(result.getStatus())) {
                        logger.warn("Calculation resulted in error for RequestId {}. Response: {}.", requestId, result.getPayload());
                        return ResponseEntity.badRequest().header("X-Request-Id", requestId).body(result);
                    }
                    
                    logger.info("OK response for RequestId {}. Result: {}.", requestId, result.getPayload());
                    return ResponseEntity.ok().header("X-Request-Id", requestId).body(result);
                } finally {
                    MDC.clear();
                }
            })
            .exceptionally(ex -> {
                if (ex.getCause() instanceof java.util.concurrent.TimeoutException) {
                    logger.error("Request with RequestId {} exceeded the time limit.", requestId);
                    return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
                        .header("X-Request-Id", requestId)
                        .body(new CalculationDTO("ERROR", "The request exceeded the time limit."));
                } else {
                    logger.error("An error occurred while processing the request with RequestId {}.", requestId, ex);
                    return ResponseEntity.internalServerError()
                        .header("X-Request-Id", requestId)
                        .body(new CalculationDTO("ERROR", "Internal server error."));
                }
            });
    }
}