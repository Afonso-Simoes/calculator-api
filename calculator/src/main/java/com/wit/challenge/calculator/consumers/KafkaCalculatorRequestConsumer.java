package com.wit.challenge.calculator.consumers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import com.wit.challenge.calculator.producers.KafkaCalculatorResultProducer;
import com.wit.challenge.calculator.services.CalculatorService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;

@Component
public class KafkaCalculatorRequestConsumer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaCalculatorRequestConsumer.class);

    private final CalculatorService calculatorService;
    private final KafkaCalculatorResultProducer kafkaCalculatorResultProducer;

    public KafkaCalculatorRequestConsumer(CalculatorService calculatorService, KafkaCalculatorResultProducer kafkaCalculatorResultProducer) {
        this.calculatorService = calculatorService;
        this.kafkaCalculatorResultProducer = kafkaCalculatorResultProducer;
    }

    @KafkaListener(topics = "calculator.request", groupId = "request-group")
    public void consume(String message) {
        logger.info("Request message received from Kafka: {}", message);
        String[] parts = message.split(",");

        if (parts.length != 4) {
            logger.error("Request message has an invalid format: {}. Ignoring.", message);
            return;
        }

        String operation = parts[0];
        BigDecimal a = new BigDecimal(parts[1]);
        BigDecimal b = new BigDecimal(parts[2]);
        String requestId = parts[3];

        try {
            MDC.put("requestId", requestId);
            
            logger.info("Starting calculation of operation '{}' for RequestId: {}.", operation, requestId);

            BigDecimal result;
            String status = "OK";
            String payload;

            switch (operation) {
                case "sum":
                    result = calculatorService.sum(a, b);
                    payload = result.toString();
                    break;
                case "subtract":
                    result = calculatorService.subtract(a, b);
                    payload = result.toString();
                    break;
                case "multiply":
                    result = calculatorService.multiply(a, b);
                    payload = result.toString();
                    break;
                case "divide":
                    result = calculatorService.divide(a, b, MathContext.DECIMAL128);
                    payload = result.toString();
                    break;
                default:
                    status = "ERROR";
                    payload = "Invalid operation: " + operation;
                    logger.error("Invalid operation '{}' for RequestId: {}.", operation, requestId);
                    break;
            }
            logger.info("Calculation for RequestId {} completed. Status: {}, Result: {}", requestId, status, payload);
            kafkaCalculatorResultProducer.send(requestId, status, payload);
        } catch (IllegalArgumentException e) {
            String status = "ERROR";
            String payload = e.getMessage();
            logger.error("Calculation error for RequestId {}: {}", requestId, payload);
            kafkaCalculatorResultProducer.send(requestId, status, payload);
        } catch (Exception e) {
            String status = "ERROR";
            String payload = "Ocorreu um erro interno: " + e.getMessage();
            logger.error("Internal error for RequestId {}: {}", requestId, payload, e);
            kafkaCalculatorResultProducer.send(requestId, status, payload);
        } finally {
            MDC.clear();
        }
    }
}