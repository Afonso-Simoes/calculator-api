package com.wit.challenge.rest.controllers;

import com.wit.challenge.rest.consumers.KafkaCalculatorResultConsumer;
import com.wit.challenge.rest.dtos.CalculationDTO;
import com.wit.challenge.rest.producers.KafkaCalculatorRequestProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasProperty;
import java.util.concurrent.CompletableFuture;
import static org.hamcrest.Matchers.is;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;

@WebMvcTest(CalculatorController.class)
public class CalculatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean 
    private KafkaCalculatorRequestProducer kafkaCalculatorRequestProducer;

    @MockitoBean
    private KafkaCalculatorResultConsumer kafkaCalculatorResultConsumer;
   
    @Test
    void shouldReturnBadRequestForInvalidOperation() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/invalid_op").param("a", "1").param("b", "2"))
                .andExpect(request().asyncStarted())
                .andReturn();
                
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.payload").value("Invalid operation. Valid operations are: sum, subtract, multiply, divide."));

        verifyNoInteractions(kafkaCalculatorRequestProducer);
        verifyNoInteractions(kafkaCalculatorResultConsumer);
    }

    @Test
    void shouldReturnBadRequestForNonNumericParameters() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/sum").param("a", "abc").param("b", "2"))
                .andExpect(request().asyncStarted())
                .andReturn();
                
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.payload").value("Parameters 'a' and 'b' must be valid numbers."));

        verifyNoInteractions(kafkaCalculatorRequestProducer);
        verifyNoInteractions(kafkaCalculatorResultConsumer);
    }

    @Test
    void shouldProcessValidCalculationRequestAndReturnOk() throws Exception {
        String requestId = "test-uuid-123";
        CalculationDTO resultDto = new CalculationDTO("OK", "5.0");

        when(kafkaCalculatorRequestProducer.send(any(), any(), any())).thenReturn(requestId);

        doAnswer(invocation -> {
            CompletableFuture<CalculationDTO> future = invocation.getArgument(1);
            future.complete(resultDto);
            return null;
        }).when(kafkaCalculatorResultConsumer).registerRequest(eq(requestId), any(CompletableFuture.class));

        mockMvc.perform(get("/sum").param("a", "2").param("b", "3"))
            .andExpect(request().asyncStarted())
            .andExpect(request().asyncResult(hasProperty("body", is(resultDto))))
            .andExpect(status().isOk());

        verify(kafkaCalculatorRequestProducer, times(1)).send(eq("sum"), eq("2"), eq("3"));
        verify(kafkaCalculatorResultConsumer, times(1)).registerRequest(eq(requestId), any(CompletableFuture.class));
    }
}