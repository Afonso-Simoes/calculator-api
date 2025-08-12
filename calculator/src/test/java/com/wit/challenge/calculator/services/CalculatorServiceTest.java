package com.wit.challenge.calculator.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.math.MathContext;

import static org.junit.jupiter.api.Assertions.*;

class CalculatorServiceTest {

    private CalculatorService calculatorService;

    @BeforeEach
    void setUp() {
        calculatorService = new CalculatorService();
    }

    @Test
    void testSum() {
        BigDecimal a = new BigDecimal("10.5");
        BigDecimal b = new BigDecimal("5.2");
        BigDecimal expected = new BigDecimal("15.7");
        BigDecimal actual = calculatorService.sum(a, b);
        assertEquals(0, expected.compareTo(actual), "The sum should be 15.7");
    }

    @Test
    void testSubtract() {
        BigDecimal a = new BigDecimal("20.0");
        BigDecimal b = new BigDecimal("7.5");
        BigDecimal expected = new BigDecimal("12.5");
        BigDecimal actual = calculatorService.subtract(a, b);
        assertEquals(0, expected.compareTo(actual), "The subtraction should be 12.5");
    }

    @Test
    void testMultiply() {
        BigDecimal a = new BigDecimal("4.0");
        BigDecimal b = new BigDecimal("2.5");
        BigDecimal expected = new BigDecimal("10.0");
        BigDecimal actual = calculatorService.multiply(a, b);
        assertEquals(0, expected.compareTo(actual), "The multiplication should be 10.0");
    }

    @Test
    void testDivide() {
        BigDecimal a = new BigDecimal("10.0");
        BigDecimal b = new BigDecimal("2.0");
        BigDecimal expected = new BigDecimal("5.0");
        BigDecimal actual = calculatorService.divide(a, b, MathContext.DECIMAL128);
        assertEquals(0, expected.compareTo(actual), "The division should be 5.0");
    }

    @Test
    void testDivideByZero() {
        BigDecimal a = new BigDecimal("10.0");
        BigDecimal b = BigDecimal.ZERO;

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            calculatorService.divide(a, b, MathContext.DECIMAL128);
        });

        String expectedMessage = "Division by zero is not allowed.";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage), "The error message should indicate division by zero.");
    }
}