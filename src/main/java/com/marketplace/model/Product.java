package com.marketplace.model;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.util.List;

public class Product {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    @SerializedName("old_price")
    private BigDecimal oldPrice;
    @SerializedName("category_id")
    private String categoryId;
    private String category;
    private double rating;
    @SerializedName("reviews_count")
    private int reviewsCount;
    private int stock;
    @SerializedName("seller_id")
    private String sellerId;
    @SerializedName("seller_name")
    private String sellerName;
    @SerializedName("image_urls")
    private List<String> imageUrls;
    @SerializedName("is_active")
    private boolean isActive;
    @SerializedName("created_at")
    private String createdAt;
    @SerializedName("sales_count")
    private int salesCount;
    private String brand;
    private String sku;
    @SerializedName("weight_kg")
    private Double weightKg;
    @SerializedName("is_approved")
    private boolean isApproved;


    private String article;

    @SerializedName("supplier_country")
    private String supplierCountry;

    @SerializedName("delivery_days_min")
    private Integer deliveryDaysMin;

    @SerializedName("delivery_days_max")
    private Integer deliveryDaysMax;

    public Product() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getOldPrice() { return oldPrice; }
    public void setOldPrice(BigDecimal oldPrice) { this.oldPrice = oldPrice; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getReviewsCount() { return reviewsCount; }
    public void setReviewsCount(int reviewsCount) { this.reviewsCount = reviewsCount; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public boolean isApproved() { return isApproved; }
    public void setApproved(boolean approved) { isApproved = approved; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public int getSalesCount() { return salesCount; }
    public void setSalesCount(int salesCount) { this.salesCount = salesCount; }

    public int getSold() { return salesCount; }
    public void setSold(int sold) { this.salesCount = sold; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public Double getWeightKg() { return weightKg; }
    public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }


    public String getArticle() { return article; }
    public void setArticle(String article) { this.article = article; }

    public String getSupplierCountry() { return supplierCountry; }
    public void setSupplierCountry(String supplierCountry) { this.supplierCountry = supplierCountry; }

    public Integer getDeliveryDaysMin() { return deliveryDaysMin; }
    public void setDeliveryDaysMin(Integer deliveryDaysMin) { this.deliveryDaysMin = deliveryDaysMin; }

    public Integer getDeliveryDaysMax() { return deliveryDaysMax; }
    public void setDeliveryDaysMax(Integer deliveryDaysMax) { this.deliveryDaysMax = deliveryDaysMax; }

    public String getDeliveryLabel() {
        int min = deliveryDaysMin != null ? deliveryDaysMin : 3;
        int max = deliveryDaysMax != null ? deliveryDaysMax : 7;
        if (min == max) return min + " дн.";
        return min + "–" + max + " дн.";
    }

    public String getFirstImageUrl() {
        return (imageUrls != null && !imageUrls.isEmpty()) ? imageUrls.get(0) : null;
    }

    public int getDiscountPercent() {
        if (oldPrice == null || oldPrice.compareTo(BigDecimal.ZERO) == 0) return 0;
        return (int) ((oldPrice.subtract(price))
                .divide(oldPrice, 2, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue());
    }
}
