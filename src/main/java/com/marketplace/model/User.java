package com.marketplace.model;

import com.google.gson.annotations.SerializedName;

public class User {
    private String id;
    private String email;
    @SerializedName("full_name")
    private String fullName;
    private String phone;
    private String role;
    @SerializedName("is_blocked")
    private boolean isBlocked;
    @SerializedName("avatar_url")
    private String avatarUrl;
    @SerializedName("created_at")
    private String createdAt;

    public User() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isBlocked() { return isBlocked; }
    public void setBlocked(boolean blocked) { isBlocked = blocked; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public boolean isAdmin() { return "admin".equals(role); }
    public boolean isSeller() { return "seller".equals(role) || "admin".equals(role); }
}
