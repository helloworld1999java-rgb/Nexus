package com.marketplace.service;

import com.google.gson.*;
import com.marketplace.config.AppState;
import com.marketplace.config.SupabaseConfig;
import com.marketplace.model.CartItem;
import com.marketplace.model.Product;
import java.util.ArrayList;
import java.util.List;

public class CartService {
    private static final Gson gson = new Gson();
    private final SupabaseClient client = SupabaseClient.getInstance();
    private final ProductService productService = new ProductService();

    public List<CartItem> getCartItems() throws Exception {
        String userId = AppState.getInstance().getCurrentUser().getId();
        String url = SupabaseConfig.REST_URL + "/cart_items?user_id=eq." + userId + "&select=*,product:products(*)";
        String response = client.get(url);
        JsonArray arr = JsonParser.parseString(response).getAsJsonArray();
        List<CartItem> items = new ArrayList<>();
        for (JsonElement el : arr) {
            JsonObject obj = el.getAsJsonObject();
            CartItem item = gson.fromJson(obj, CartItem.class);
            if (obj.has("product") && !obj.get("product").isJsonNull()) {
                Product p = gson.fromJson(obj.get("product"), Product.class);
                item.setProduct(p);
            }
            items.add(item);
        }
        return items;
    }

    public void addToCart(String productId, int quantity) throws Exception {
        String userId = AppState.getInstance().getCurrentUser().getId();
        String checkUrl = SupabaseConfig.REST_URL + "/cart_items?user_id=eq." + userId
                + "&product_id=eq." + productId + "&select=id,quantity";
        String checkResp = client.get(checkUrl);
        JsonArray arr = JsonParser.parseString(checkResp).getAsJsonArray();

        if (arr.size() > 0) {
            String itemId = arr.get(0).getAsJsonObject().get("id").getAsString();
            int currentQty = arr.get(0).getAsJsonObject().get("quantity").getAsInt();
            JsonObject body = new JsonObject();
            body.addProperty("quantity", currentQty + quantity);
            client.patch(SupabaseConfig.REST_URL + "/cart_items?id=eq." + itemId, gson.toJson(body));
        } else {
            JsonObject body = new JsonObject();
            body.addProperty("user_id", userId);
            body.addProperty("product_id", productId);
            body.addProperty("quantity", quantity);
            client.post(SupabaseConfig.REST_URL + "/cart_items", gson.toJson(body));
        }
    }

    public void updateQuantity(String itemId, int quantity) throws Exception {
        if (quantity <= 0) {
            removeFromCart(itemId);
            return;
        }
        JsonObject body = new JsonObject();
        body.addProperty("quantity", quantity);
        client.patch(SupabaseConfig.REST_URL + "/cart_items?id=eq." + itemId, gson.toJson(body));
    }

    public void removeFromCart(String itemId) throws Exception {
        client.delete(SupabaseConfig.REST_URL + "/cart_items?id=eq." + itemId);
    }

    public void clearCart() throws Exception {
        String userId = AppState.getInstance().getCurrentUser().getId();
        client.delete(SupabaseConfig.REST_URL + "/cart_items?user_id=eq." + userId);
    }

    public int getCartCount() throws Exception {
        String userId = AppState.getInstance().getCurrentUser().getId();
        String url = SupabaseConfig.REST_URL + "/cart_items?user_id=eq." + userId + "&select=quantity";
        String response = client.get(url);
        JsonArray arr = JsonParser.parseString(response).getAsJsonArray();
        int count = 0;
        for (JsonElement el : arr) count += el.getAsJsonObject().get("quantity").getAsInt();
        return count;
    }
}
