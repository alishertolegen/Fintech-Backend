package com.fintech.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;
@Document(collection = "startups")
public class Startup {

    @Id
    private String id;

    private String name;

    @Indexed(unique = true)
    private String slug;               // уникальный краткий идентификатор

    private String founderId;          // ref -> users._id (String)

    private String stage;              // "idea","incubation","seed","growth"
    private String industry;
    private String shortPitch;
    private String description;
    private String website;
    private String logoUrl;

    private MetricsSnapshot metricsSnapshot; // кеш
    private List<String> attachments;        // id файлов (string ObjectId)

    private Instant createdAt;
    private Instant updatedAt;

    private String visibility;         // "public" | "private"

    private String valuationMode; // "pre" или "post"
    private List<String> images;
    public Startup() {}

    public static class MetricsSnapshot {

        private Double mrr;

        // Было users → добавим activeUsers для совместимости
        private Integer users;          // старое поле
        private Integer activeUsers;    // новое поле (для контроллера)

        private Double burnRate;        // новое поле

        private Double valuationPreMoney;
        private Double valuationPostMoney;

        private Map<String, Object> other; // новое поле (произвольные метрики)

        public MetricsSnapshot() {}

        public MetricsSnapshot(Double mrr, Integer users) {
            this.mrr = mrr;
            this.users = users;
            this.activeUsers = users; // синхронизация
        }

        public MetricsSnapshot(Double mrr,
                               Integer users,
                               Double valuationPreMoney,
                               Double valuationPostMoney) {
            this.mrr = mrr;
            this.users = users;
            this.activeUsers = users;
            this.valuationPreMoney = valuationPreMoney;
            this.valuationPostMoney = valuationPostMoney;
        }

        // --- MRR ---
        public Double getMrr() { return mrr; }
        public void setMrr(Double mrr) { this.mrr = mrr; }

        // --- Users (старое поле) ---
        public Integer getUsers() { return users; }
        public void setUsers(Integer users) {
            this.users = users;
            this.activeUsers = users; // держим синхрон
        }

        // --- ActiveUsers (новое поле) ---
        public Integer getActiveUsers() {
            return activeUsers != null ? activeUsers : users;
        }

        public void setActiveUsers(Integer activeUsers) {
            this.activeUsers = activeUsers;
            this.users = activeUsers; // синхрон с legacy полем
        }

        // --- Burn Rate ---
        public Double getBurnRate() { return burnRate; }
        public void setBurnRate(Double burnRate) { this.burnRate = burnRate; }

        // --- Valuation ---
        public Double getValuationPreMoney() { return valuationPreMoney; }
        public void setValuationPreMoney(Double valuationPreMoney) {
            this.valuationPreMoney = valuationPreMoney;
        }

        public Double getValuationPostMoney() { return valuationPostMoney; }
        public void setValuationPostMoney(Double valuationPostMoney) {
            this.valuationPostMoney = valuationPostMoney;
        }

        // --- Other ---
        public Map<String, Object> getOther() { return other; }
        public void setOther(Map<String, Object> other) { this.other = other; }
    }

    // --- getters / setters ---
    public String getValuationMode() {
        return valuationMode;
    }

    public void setValuationMode(String valuationMode) {
        this.valuationMode = valuationMode;
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getFounderId() { return founderId; }
    public void setFounderId(String founderId) { this.founderId = founderId; }

    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public String getShortPitch() { return shortPitch; }
    public void setShortPitch(String shortPitch) { this.shortPitch = shortPitch; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public MetricsSnapshot getMetricsSnapshot() { return metricsSnapshot; }
    public void setMetricsSnapshot(MetricsSnapshot metricsSnapshot) { this.metricsSnapshot = metricsSnapshot; }

    public List<String> getAttachments() { return attachments; }
    public void setAttachments(List<String> attachments) { this.attachments = attachments; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
}
