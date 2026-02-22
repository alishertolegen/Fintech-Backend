package com.fintech.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Document(collection = "offers")
public class Offer {
    @Id
    private String id;

    private String startupId;
    private String investorId;
    private String title;
    private Integer amount; // хранить в целых единицах (например в долларах)
    private Double equityPercent;
    private String type; // "offer" | "term-sheet"
    private String visibility; // "private" | "public"
    private String status; // "sent" | "accepted" | "rejected" | "countered"
    private List<Attachment> attachments = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;
    private String note;

    public Offer() {}

    // Конструктор для быстрого создания
    public Offer(String startupId, String investorId, String title, Integer amount, Double equityPercent,
                 String type, String visibility, String status, List<Attachment> attachments, String note) {
        this.startupId = startupId;
        this.investorId = investorId;
        this.title = title;
        this.amount = amount;
        this.equityPercent = equityPercent;
        this.type = type;
        this.visibility = visibility;
        this.status = status;
        this.attachments = attachments == null ? new ArrayList<>() : attachments;
        this.note = note;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // --- getters / setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStartupId() { return startupId; }
    public void setStartupId(String startupId) { this.startupId = startupId; }

    public String getInvestorId() { return investorId; }
    public void setInvestorId(String investorId) { this.investorId = investorId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }

    public Double getEquityPercent() { return equityPercent; }
    public void setEquityPercent(Double equityPercent) { this.equityPercent = equityPercent; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<Attachment> getAttachments() { return attachments; }
    public void setAttachments(List<Attachment> attachments) { this.attachments = attachments == null ? new ArrayList<>() : attachments; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    // Вложенный класс для вложений
    public static class Attachment {
        private String url;
        private String name;

        public Attachment() {}
        public Attachment(String url, String name) { this.url = url; this.name = name; }

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Attachment)) return false;
            Attachment that = (Attachment) o;
            return Objects.equals(url, that.url) && Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(url, name);
        }
    }
}
