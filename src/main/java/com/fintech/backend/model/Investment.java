package com.fintech.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection = "investments")
public class Investment {
    @Id
    private String id;
    private String startupId;
    private String investorId;
    private int amount;
    private String currency;
    private double equityPercent;
    private Double valuationPostMoney; // <- changed to Double
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
    private String note;

    public Investment() {}

    public Investment(String startupId, String investorId, int amount, String currency,
                      double equityPercent, Double valuationPostMoney, String status,
                      Instant createdAt, Instant updatedAt, String note) {
        this.startupId = startupId;
        this.investorId = investorId;
        this.amount = amount;
        this.currency = currency;
        this.equityPercent = equityPercent;
        this.valuationPostMoney = valuationPostMoney;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.note = note;
    }

    // Getters and setters
    public String getId() { return id; }
    public String getStartupId() { return startupId; }
    public void setStartupId(String startupId) { this.startupId = startupId; }
    public String getInvestorId() { return investorId; }
    public void setInvestorId(String investorId) { this.investorId = investorId; }
    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public double getEquityPercent() { return equityPercent; }
    public void setEquityPercent(double equityPercent) { this.equityPercent = equityPercent; }
    public Double getValuationPostMoney() { return valuationPostMoney; } // <- Double
    public void setValuationPostMoney(Double valuationPostMoney) { this.valuationPostMoney = valuationPostMoney; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
