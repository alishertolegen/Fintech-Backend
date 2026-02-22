package com.fintech.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "investors")
public class Investor {

    @Id
    private String id;

    // Ссылка на user (ObjectId в БД, но хранится как строка)
    private String userId;

    private String legalName;
    private String type; // "angel" | "vc" | "corporate"
    private Integer minCheck;
    private Integer maxCheck;
    private List<String> preferredIndustries;
    private List<String> preferredStages;
    private String description;
    private String website;
    private Boolean isVerified;
    private Instant createdAt;
    private Instant updatedAt;

    public Investor() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.isVerified = false;
    }

    // getters / setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getLegalName() { return legalName; }
    public void setLegalName(String legalName) { this.legalName = legalName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getMinCheck() { return minCheck; }
    public void setMinCheck(Integer minCheck) { this.minCheck = minCheck; }

    public Integer getMaxCheck() { return maxCheck; }
    public void setMaxCheck(Integer maxCheck) { this.maxCheck = maxCheck; }

    public List<String> getPreferredIndustries() { return preferredIndustries; }
    public void setPreferredIndustries(List<String> preferredIndustries) { this.preferredIndustries = preferredIndustries; }

    public List<String> getPreferredStages() { return preferredStages; }
    public void setPreferredStages(List<String> preferredStages) { this.preferredStages = preferredStages; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean verified) { isVerified = verified; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
