package com.fintech.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.Map;

@Document(collection = "startup_metrics")
@CompoundIndex(def = "{'startupId': 1, 'date': -1}", name = "idx_startup_date")
public class StartupMetric {

    @Id
    private String id;
    private String startupId;
    private Instant date;

    private Double mrr;
    private Integer activeUsers;
    private Double burnRate;

    // Добавленные поля оценки
    private Double valuationPreMoney;
    private Double valuationPostMoney;

    @Field("other")
    private Map<String, Object> other;

    private Instant createdAt;
    private Instant updatedAt;

    public StartupMetric() {}

    // --- getters / setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStartupId() { return startupId; }
    public void setStartupId(String startupId) { this.startupId = startupId; }

    public Instant getDate() { return date; }
    public void setDate(Instant date) { this.date = date; }

    public Double getMrr() { return mrr; }
    public void setMrr(Double mrr) { this.mrr = mrr; }

    public Integer getActiveUsers() { return activeUsers; }
    public void setActiveUsers(Integer activeUsers) { this.activeUsers = activeUsers; }

    public Double getBurnRate() { return burnRate; }
    public void setBurnRate(Double burnRate) { this.burnRate = burnRate; }

    // геттер/сеттер для valuationPreMoney
    public Double getValuationPreMoney() { return valuationPreMoney; }
    public void setValuationPreMoney(Double valuationPreMoney) { this.valuationPreMoney = valuationPreMoney; }

    // геттер/сеттер для valuationPostMoney
    public Double getValuationPostMoney() { return valuationPostMoney; }
    public void setValuationPostMoney(Double valuationPostMoney) { this.valuationPostMoney = valuationPostMoney; }

    public Map<String, Object> getOther() { return other; }
    public void setOther(Map<String, Object> other) { this.other = other; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}