package com.fintech.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection = "users")
public class User {

    @Id
    private String id;

    private String email;         // unique, lowercase
    private String passwordHash;  // bcrypt
    private String role;          // "founder" | "investor" | "admin"
    private String name;
    private String company;
    private String bio;
    private String avatarUrl;
    private Instant createdAt;
    private Instant updatedAt;
    private Boolean isVerified = false;
    private Meta meta;

    public User() {}

    public User(String email, String passwordHash, String role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public static class Meta {
        private String phone;
        private String location;

        public Meta() {}
        public Meta(String phone, String location) { this.phone = phone; this.location = location; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }

    // --- getters / setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }

    public Meta getMeta() { return meta; }
    public void setMeta(Meta meta) { this.meta = meta; }
}
