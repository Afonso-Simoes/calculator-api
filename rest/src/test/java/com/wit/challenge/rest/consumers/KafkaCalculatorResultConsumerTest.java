package com.wit.challenge.rest.consumers;

import com.wit.challenge.rest.dtos.CalculationDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

class KafkaCalculatorResultConsumerTest {

    private KafkaCalculatorResultConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new KafkaCalculatorResultConsumer();
    }

    @Test
    void testConsumeWithPendingRequest() throws Exception {
        String requestId = "test-uuid-123";
        CompletableFuture<CalculationDTO> future = new CompletableFuture<>();
        
        consumer.registerRequest(requestId, future);
        
        String message = String.format("%s,%s,%s", requestId, "OK", "15.0");
        consumer.consume(message);
        
        CalculationDTO result = future.get(1, TimeUnit.SECONDS);
        
        assertNotNull(result);
        assertEquals("OK", result.getStatus());
        assertEquals("15.0", result.getPayload());
    }

    @Test
    void testConsumeWithErrorMessage() throws Exception {
        String requestId = "test-uuid-456";
        CompletableFuture<CalculationDTO> future = new CompletableFuture<>();
        consumer.registerRequest(requestId, future);
        
        String message = String.format("%s,%s,%s", requestId, "ERROR", "Division by zero");
        consumer.consume(message);
        
        CalculationDTO result = future.get(1, TimeUnit.SECONDS);
        
        assertNotNull(result);
        assertEquals("ERROR", result.getStatus());
        assertEquals("Division by zero", result.getPayload());
    }

    @Test
    void testConsumeWithoutPendingRequest() throws Exception {
        String requestId = "non-existent-uuid";
        String message = String.format("%s,%s,%s", requestId, "OK", "100.0");
        
        consumer.consume(message);
        
        assertTrue(true); 
    }

    @Test
    void testConsumeWithInvalidMessageFormat() {
        String invalidMessage = "invalid_format";
        
        assertDoesNotThrow(() -> consumer.consume(invalidMessage));
    }
}