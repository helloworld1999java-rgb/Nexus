package com.marketplace.model;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class Order {
    private String id;
    @SerializedName("user_id")
    private String userId;
    @SerializedName("address_id")
    private String addressId;
    @SerializedName("card_id")
    private String cardId;
    @SerializedName("promo_code")
    private String promoCode;
    @SerializedName("discount_amount")
    private BigDecimal discountAmount;
    @SerializedName("total_amount")
    private BigDecimal totalAmount;
    private String status;
    @SerializedName("created_at")
    private String createdAt;
    @SerializedName("updated_at")
    private String updatedAt;
    @SerializedName("tracking_number")
    private String trackingNumber;

    @SerializedName("estimated_delivery")
    private String estimatedDelivery;

    private List<OrderItem> items;
    private Address address;

    public Order() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getAddressId() { return addressId; }
    public void setAddressId(String addressId) { this.addressId = addressId; }
    public String getCardId() { return cardId; }
    public void setCardId(String cardId) { this.cardId = cardId; }
    public String getPromoCode() { return promoCode; }
    public void setPromoCode(String promoCode) { this.promoCode = promoCode; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    public String getEstimatedDelivery() { return estimatedDelivery; }
    public void setEstimatedDelivery(String estimatedDelivery) { this.estimatedDelivery = estimatedDelivery; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }

    public String getStatusLabel() {
        return switch (status != null ? status : "") {
            case "created"    -> "Создан";
            case "processing" -> "В обработке";
            case "shipped"    -> "Отправлен";
            case "delivered"  -> "Доставлен";
            case "cancelled"  -> "Отменён";
            default           -> "Неизвестно";
        };
    }

    public String getStatusStyle() {
        return switch (status != null ? status : "") {
            case "created"    -> "status-created";
            case "processing" -> "status-processing";
            case "shipped"    -> "status-shipped";
            case "delivered"  -> "status-delivered";
            case "cancelled"  -> "status-cancelled";
            default           -> "";
        };
    }

    public String getShortId() {
        return id != null ? "#" + id.substring(0, 8).toUpperCase() : "";
    }

    public String getDeliveryStatusText() {
        if (estimatedDelivery == null || estimatedDelivery.isEmpty()) return null;
        if ("cancelled".equals(status)) return null;
        try {
            LocalDate delivery = LocalDate.parse(estimatedDelivery);
            LocalDate today    = LocalDate.now();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d MMMM", new Locale("ru"));
            String dateStr = delivery.format(fmt);
            if ("delivered".equals(status)) {
                return "📦 Доставлен";
            }
            if (!today.isAfter(delivery)) {
                return "🚚 Ожидается " + dateStr;
            } else {
                return "📦 Товар приехал " + dateStr;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public String getDeliveryStatusStyle() {
        if (estimatedDelivery == null || estimatedDelivery.isEmpty()) return "-fx-text-fill: #6c757d;";
        try {
            LocalDate delivery = LocalDate.parse(estimatedDelivery);
            LocalDate today    = LocalDate.now();
            if ("delivered".equals(status) || today.isAfter(delivery)) {
                return "-fx-text-fill: #16a34a; -fx-font-weight: bold;";
            } else {
                return "-fx-text-fill: #2563eb;";
            }
        } catch (Exception e) {
            return "-fx-text-fill: #6c757d;";
        }
    }
}
