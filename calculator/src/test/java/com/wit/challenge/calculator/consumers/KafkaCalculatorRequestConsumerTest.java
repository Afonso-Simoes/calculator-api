package com.wit.challenge.calculator.consumers;

import com.wit.challenge.calculator.producers.KafkaCalculatorResultProducer;
import com.wit.challenge.calculator.services.CalculatorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.MathContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KafkaCalculatorRequestConsumerTest {

    @Mock
    private CalculatorService calculatorService;

    @Mock
    private KafkaCalculatorResultProducer kafkaCalculatorResultProducer;

    @InjectMocks
    private KafkaCalculatorRequestConsumer consumer;

    @Test
    void testConsumeForSum() {
        when(calculatorService.sum(any(BigDecimal.class), any(BigDecimal.class))).thenReturn(new BigDecimal("30"));

        consumer.consume("sum,10,20,test-uuid-sum");

        verify(kafkaCalculatorResultProducer, times(1)).send(eq("test-uuid-sum"), eq("OK"), eq("30"));
    }

    @Test
    void testConsumeForInvalidOperation() {
        consumer.consume("unknown,10,20,test-uuid-invalid");

        verify(kafkaCalculatorResultProducer, times(1)).send(eq("test-uuid-invalid"), eq("ERROR"), eq("Invalid operation: unknown"));
    }
    
    @Test
    void testConsumeForDivideByZeroException() {
        when(calculatorService.divide(any(BigDecimal.class), eq(BigDecimal.ZERO), any(MathContext.class)))
            .thenThrow(new IllegalArgumentException("Division by zero is not allowed."));

        consumer.consume("divide,10,0,test-uuid-divide-by-zero");

        verify(kafkaCalculatorResultProducer, times(1)).send(eq("test-uuid-divide-by-zero"), eq("ERROR"), eq("Division by zero is not allowed."));
    }

    @Test
    void testConsumeForInvalidMessageFormat() {
        consumer.consume("invalid-message");

        verify(kafkaCalculatorResultProducer, never()).send(any(), any(), any());
    }
}