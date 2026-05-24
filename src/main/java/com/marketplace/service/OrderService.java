package com.marketplace.service;

import com.google.gson.*;
import com.marketplace.config.AppState;
import com.marketplace.config.SupabaseConfig;
import com.marketplace.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderService {
    private static final Gson gson = new GsonBuilder().serializeNulls().create();
    private final SupabaseClient client = SupabaseClient.getInstance();
    private final CartService cartService = new CartService();

    public Order createOrder(String addressId, String cardId, String promoCode,
                             BigDecimal discountAmount, BigDecimal totalAmount,
                             List<CartItem> cartItems) throws Exception {
        String userId = AppState.getInstance().getCurrentUser().getId();

        int maxDays = 7;
        for (CartItem ci : cartItems) {
            if (ci.getProduct() != null) {
                int days = ci.getProduct().getDeliveryDaysMax() != null
                        ? ci.getProduct().getDeliveryDaysMax() : 7;
                if (days > maxDays) maxDays = days;
            }
        }
        String estimatedDelivery = LocalDate.now().plusDays(maxDays).toString(); // YYYY-MM-DD

        JsonObject orderBody = new JsonObject();
        orderBody.addProperty("user_id", userId);
        orderBody.addProperty("address_id", addressId);
        orderBody.addProperty("card_id", cardId);
        if (promoCode != null) orderBody.addProperty("promo_code", promoCode);
        orderBody.addProperty("discount_amount", discountAmount);
        orderBody.addProperty("total_amount", totalAmount);
        orderBody.addProperty("final_amount", totalAmount);
        orderBody.addProperty("status", "created");
        orderBody.addProperty("estimated_delivery", estimatedDelivery);
        orderBody.addProperty("tracking_number",
                "TRK" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        String orderResp = client.postService(SupabaseConfig.REST_URL + "/orders?select=*", gson.toJson(orderBody));
        JsonArray orderArr = JsonParser.parseString(orderResp).getAsJsonArray();
        if (orderArr.size() == 0) throw new Exception("Ошибка создания заказа");
        Order order = gson.fromJson(orderArr.get(0), Order.class);

        JsonArray itemsArr = new JsonArray();
        for (CartItem ci : cartItems) {
            JsonObject item = new JsonObject();
            item.addProperty("order_id", order.getId());
            item.addProperty("product_id", ci.getProductId());
            item.addProperty("product_name", ci.getProduct() != null ? ci.getProduct().getName() : "");
            item.addProperty("product_image", ci.getProduct() != null ? ci.getProduct().getFirstImageUrl() : "");
            item.addProperty("quantity", ci.getQuantity());
            item.addProperty("unit_price",
                    ci.getProduct() != null ? ci.getProduct().getPrice() : BigDecimal.ZERO);
            item.addProperty("subtotal", ci.getTotalPrice());
            itemsArr.add(item);
        }
        client.postService(SupabaseConfig.REST_URL + "/order_items", gson.toJson(itemsArr));

        for (CartItem ci : cartItems) {
            cartService.removeFromCart(ci.getId());
        }

        return order;
    }

    public List<Order> getUserOrders() throws Exception {
        String userId = AppState.getInstance().getCurrentUser().getId();

        String ordersUrl = SupabaseConfig.REST_URL
                + "/orders?user_id=eq." + userId
                + "&select=*,address:addresses(*)"
                + "&order=created_at.desc";
        String ordersResp = client.get(ordersUrl);
        List<Order> orders = parseOrdersWithoutItems(JsonParser.parseString(ordersResp).getAsJsonArray());

        if (!orders.isEmpty()) {
            enrichOrdersWithItems(orders);
        }

        return orders;
    }

    public List<Order> getAllOrders() throws Exception {
        String url = SupabaseConfig.REST_URL
                + "/orders?select=*,address:addresses(*)"
                + "&order=created_at.desc&limit=100";
        String response = client.get(url);
        List<Order> orders = parseOrdersWithoutItems(JsonParser.parseString(response).getAsJsonArray());
        if (!orders.isEmpty()) {
            enrichOrdersWithItems(orders);
        }
        return orders;
    }

    public Order getOrderById(String orderId) throws Exception {
        String url = SupabaseConfig.REST_URL
                + "/orders?id=eq." + orderId
                + "&select=*,address:addresses(*)";
        String response = client.get(url);
        JsonArray arr = JsonParser.parseString(response).getAsJsonArray();
        if (arr.size() == 0) throw new Exception("Заказ не найден");

        Order order = parseOrderWithoutItems(arr.get(0).getAsJsonObject());
        order.setItems(getItemsForOrder(orderId));
        return order;
    }

    public void updateOrderStatus(String orderId, String status) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("status", status);
        client.patch(SupabaseConfig.REST_URL + "/orders?id=eq." + orderId, gson.toJson(body));
    }

    public void cancelOrder(String orderId) throws Exception {
        updateOrderStatus(orderId, "cancelled");
    }

    private List<OrderItem> getItemsForOrder(String orderId) throws Exception {
        String url = SupabaseConfig.REST_URL
                + "/order_items?order_id=eq." + orderId + "&select=*";
        String response = client.get(url);
        JsonArray arr = JsonParser.parseString(response).getAsJsonArray();
        List<OrderItem> items = new ArrayList<>();
        for (JsonElement el : arr) items.add(gson.fromJson(el, OrderItem.class));
        return items;
    }

    private void enrichOrdersWithItems(List<Order> orders) {
        StringBuilder ids = new StringBuilder();
        for (Order o : orders) {
            if (ids.length() > 0) ids.append(",");
            ids.append(o.getId());
        }
        try {
            String url = SupabaseConfig.REST_URL
                    + "/order_items?order_id=in.(" + ids + ")&select=*";
            String response = client.get(url);
            JsonArray arr = JsonParser.parseString(response).getAsJsonArray();

            java.util.Map<String, List<OrderItem>> map = new java.util.HashMap<>();
            for (JsonElement el : arr) {
                OrderItem item = gson.fromJson(el, OrderItem.class);
                map.computeIfAbsent(item.getOrderId(), k -> new ArrayList<>()).add(item);
            }
            for (Order o : orders) {
                o.setItems(map.getOrDefault(o.getId(), new ArrayList<>()));
            }
        } catch (Exception e) {
            for (Order o : orders) {
                if (o.getItems() == null) o.setItems(new ArrayList<>());
            }
        }
    }

    private List<Order> parseOrdersWithoutItems(JsonArray arr) {
        List<Order> orders = new ArrayList<>();
        for (JsonElement el : arr) orders.add(parseOrderWithoutItems(el.getAsJsonObject()));
        return orders;
    }

    private Order parseOrderWithoutItems(JsonObject obj) {
        Order order = gson.fromJson(obj, Order.class);
        if (obj.has("address") && !obj.get("address").isJsonNull()) {
            JsonElement addrEl = obj.get("address");
            if (addrEl.isJsonObject()) {
                order.setAddress(gson.fromJson(addrEl, Address.class));
            }
        }
        return order;
    }
}
