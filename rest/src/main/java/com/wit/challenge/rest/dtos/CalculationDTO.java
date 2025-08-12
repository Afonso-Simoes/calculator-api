package com.wit.challenge.rest.dtos;

public class CalculationDTO {
    private String status;
    private String payload;

    public CalculationDTO(String status, String payload) {
        this.status = status;
        this.payload = payload;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
}