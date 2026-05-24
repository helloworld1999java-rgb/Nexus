package com.marketplace.model;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class OrderItem {
    private String id;
    @SerializedName("order_id")
    private String orderId;
    @SerializedName("product_id")
    private String productId;
    @SerializedName("product_name")
    private String productName;
    @SerializedName("product_image")
    private String productImage;
    private int quantity;
    @SerializedName("unit_price")
    private BigDecimal unitPrice;
    @SerializedName(value = "subtotal", alternate = {"total_price"})
    private BigDecimal totalPrice;

    private Product product;

    public OrderItem() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getProductImage() { return productImage; }
    public void setProductImage(String productImage) { this.productImage = productImage; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
}
