package com.marketplace.service;

import com.google.gson.*;
import com.marketplace.config.SupabaseConfig;
import com.marketplace.model.User;
import okhttp3.OkHttpClient;
import java.util.ArrayList;
import java.util.List;

public class AdminService {
    private static final Gson gson = new Gson();
    private final SupabaseClient client = SupabaseClient.getInstance();

    public List<User> getAllUsers() throws Exception {
        String url = SupabaseConfig.REST_URL + "/profiles?select=*&order=created_at.desc&limit=1000";
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .get()
                .header("apikey", SupabaseConfig.SUPABASE_SERVICE_KEY)
                .header("Authorization", "Bearer " + SupabaseConfig.SUPABASE_SERVICE_KEY)
                .header("Content-Type", "application/json")
                .build();
        try (okhttp3.Response response = SupabaseClient.getInstance().getHttpClient().newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "[]";
            if (!response.isSuccessful()) throw new Exception("HTTP " + response.code() + ": " + body);
            JsonArray arr = JsonParser.parseString(body).getAsJsonArray();
            List<User> users = new ArrayList<>();
            for (JsonElement el : arr) users.add(gson.fromJson(el, User.class));
            return users;
        }
    }

    public void blockUser(String userId, boolean block) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("is_blocked", block);
        client.patchService(SupabaseConfig.REST_URL + "/profiles?id=eq." + userId, gson.toJson(body));
    }

    public void setUserRole(String userId, String role) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("role", role);
        client.patchService(SupabaseConfig.REST_URL + "/profiles?id=eq." + userId, gson.toJson(body));
    }

    public JsonObject getSalesStats() throws Exception {
        String ordersUrl = SupabaseConfig.REST_URL + "/orders?select=total_amount,status";
        String ordersResp = client.get(ordersUrl);
        JsonArray orders = JsonParser.parseString(ordersResp).getAsJsonArray();

        double totalRevenue = 0;
        int totalOrders = orders.size();
        int deliveredOrders = 0;

        for (JsonElement el : orders) {
            JsonObject o = el.getAsJsonObject();
            if (!o.get("total_amount").isJsonNull()) {
                totalRevenue += o.get("total_amount").getAsDouble();
            }
            if ("delivered".equals(o.get("status").getAsString())) deliveredOrders++;
        }

        String usersResp = client.get(SupabaseConfig.REST_URL + "/profiles?select=id");
        int totalUsers = JsonParser.parseString(usersResp).getAsJsonArray().size();

        String prodResp = client.get(SupabaseConfig.REST_URL + "/products?select=id");
        int totalProducts = JsonParser.parseString(prodResp).getAsJsonArray().size();

        JsonObject stats = new JsonObject();
        stats.addProperty("totalRevenue", totalRevenue);
        stats.addProperty("totalOrders", totalOrders);
        stats.addProperty("deliveredOrders", deliveredOrders);
        stats.addProperty("totalUsers", totalUsers);
        stats.addProperty("totalProducts", totalProducts);
        return stats;
    }
}
