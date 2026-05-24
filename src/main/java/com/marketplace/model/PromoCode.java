package com.marketplace.model;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class PromoCode {
    private String id;
    private String code;
    @SerializedName("discount_type")
    private String discountType;
    @SerializedName("discount_value")
    private BigDecimal discountValue;
    @SerializedName("min_order_amount")
    private BigDecimal minOrderAmount;
    @SerializedName("max_uses")
    private int maxUses;
    @SerializedName("used_count")
    private int usedCount;
    @SerializedName("valid_until")
    private String validUntil;
    @SerializedName("is_active")
    private boolean isActive;

    public PromoCode() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
    public BigDecimal getMinOrderAmount() { return minOrderAmount; }
    public void setMinOrderAmount(BigDecimal minOrderAmount) { this.minOrderAmount = minOrderAmount; }
    public int getMaxUses() { return maxUses; }
    public void setMaxUses(int maxUses) { this.maxUses = maxUses; }
    public int getUsedCount() { return usedCount; }
    public void setUsedCount(int usedCount) { this.usedCount = usedCount; }
    public String getValidUntil() { return validUntil; }
    public void setValidUntil(String validUntil) { this.validUntil = validUntil; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        if ("percent".equals(discountType)) {
            return orderAmount.multiply(discountValue).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        } else {
            return discountValue.min(orderAmount);
        }
    }
}
