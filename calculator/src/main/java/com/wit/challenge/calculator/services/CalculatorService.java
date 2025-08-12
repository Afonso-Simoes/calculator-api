package com.wit.challenge.calculator.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import java.math.BigDecimal;
import java.math.MathContext;

@Service
public class CalculatorService {
    private static final Logger logger = LoggerFactory.getLogger(CalculatorService.class);

    public BigDecimal sum(BigDecimal a, BigDecimal b) {
        logger.debug("Adding {} to {}.", a, b);
        return a.add(b);
    }

    public BigDecimal subtract(BigDecimal a, BigDecimal b) {
        logger.debug("Subtracting {} from {}.", b, a);
        return a.subtract(b);
    }

    public BigDecimal multiply(BigDecimal a, BigDecimal b) {
        logger.debug("Multiplying {} by {}.", a, b);
        return a.multiply(b);
    }

    public BigDecimal divide(BigDecimal a, BigDecimal b, MathContext mc) {
        logger.debug("Dividing {} by {}.", a, b);
        if (b.compareTo(BigDecimal.ZERO) == 0) {
            logger.error("Attempted division by zero: {} / {}.", a, b);
            throw new IllegalArgumentException("Division by zero is not allowed.");
        }
        return a.divide(b, mc);
    }
}