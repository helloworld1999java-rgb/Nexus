package com.marketplace.model;

import com.google.gson.annotations.SerializedName;

public class PaymentCard {
    private String id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("card_number_mask")
    private String cardNumberMasked;

    @SerializedName("card_holder")
    private String cardHolder;

    @SerializedName("expiry_month")
    private int expiryMonth;

    @SerializedName("expiry_year")
    private int expiryYear;

    @SerializedName("card_type")
    private String cardType;

    @SerializedName("is_default")
    private boolean isDefault;

    @SerializedName("created_at")
    private String createdAt;

    public PaymentCard() {}

    public String getId()                            { return id; }
    public void setId(String id)                     { this.id = id; }
    public String getUserId()                        { return userId; }
    public void setUserId(String userId)             { this.userId = userId; }
    public String getCardNumberMasked()              { return cardNumberMasked; }
    public void setCardNumberMasked(String n)        { this.cardNumberMasked = n; }
    public String getCardHolder()                    { return cardHolder; }
    public void setCardHolder(String h)              { this.cardHolder = h; }
    public int getExpiryMonth()                      { return expiryMonth; }
    public void setExpiryMonth(int m)                { this.expiryMonth = m; }
    public int getExpiryYear()                       { return expiryYear; }
    public void setExpiryYear(int y)                 { this.expiryYear = y; }
    public String getCardType()                      { return cardType; }
    public void setCardType(String t)                { this.cardType = t; }
    public boolean isDefault()                       { return isDefault; }
    public void setDefault(boolean d)                { this.isDefault = d; }
    public String getCreatedAt()                     { return createdAt; }
    public void setCreatedAt(String c)               { this.createdAt = c; }

    public String getDisplayNumber() {
        return "•••• •••• •••• " + (cardNumberMasked != null ? cardNumberMasked : "????");
    }

    public String getExpiryDisplay() {
        return String.format("%02d/%02d", expiryMonth, expiryYear % 100);
    }
}