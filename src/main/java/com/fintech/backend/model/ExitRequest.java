package com.fintech.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "exit_requests")
public class ExitRequest {

    @Id
    private String id;

    private String investmentId;
    private Double price;

    private String status;        // PENDING, ACCEPTED, REJECTED
    private String paymentStatus; // PENDING, PAID

    private Instant createdAt;

    private String startupId;

    // --- getters & setters ---

    public String getStartupId() {
        return startupId;
    }

    public void setStartupId(String startupId) {
        this.startupId = startupId;
    }

    public String getId() {
        return id;
    }

    public String getInvestmentId() {
        return investmentId;
    }

    public void setInvestmentId(String investmentId) {
        this.investmentId = investmentId;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}