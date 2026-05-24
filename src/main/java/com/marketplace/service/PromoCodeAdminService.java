package com.marketplace.service;

import com.google.gson.*;
import com.marketplace.config.SupabaseConfig;
import com.marketplace.model.PromoCode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PromoCodeAdminService {

    private static final Gson gson = new Gson();
    private final SupabaseClient client = SupabaseClient.getInstance();

    public List<PromoCode> getAllPromoCodes() throws Exception {
        String url = SupabaseConfig.REST_URL + "/promo_codes?select=*&order=created_at.desc";
        String resp = client.get(url);
        JsonArray arr = JsonParser.parseString(resp).getAsJsonArray();
        List<PromoCode> list = new ArrayList<>();
        for (JsonElement el : arr) list.add(gson.fromJson(el, PromoCode.class));
        return list;
    }

    public PromoCode createPromoCode(String code, String description,
                                     String discountType, BigDecimal discountValue,
                                     BigDecimal minOrderAmount, int maxUses,
                                     String validUntil) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("id", UUID.randomUUID().toString());
        body.addProperty("code", code.toUpperCase().trim());
        body.addProperty("discount_type", discountType);
        body.addProperty("discount_value", discountValue);
        if (minOrderAmount != null)
            body.addProperty("min_order_amount", minOrderAmount);
        body.addProperty("max_uses", maxUses);
        body.addProperty("used_count", 0);
        if (validUntil != null && !validUntil.isBlank())
            body.addProperty("valid_until", validUntil);
        body.addProperty("is_active", true);

        String url = SupabaseConfig.REST_URL + "/promo_codes";
        String resp = client.post(url, gson.toJson(body));
        JsonArray arr = JsonParser.parseString(resp).getAsJsonArray();
        if (arr.size() == 0) throw new Exception("Промокод не создан");
        return gson.fromJson(arr.get(0), PromoCode.class);
    }

    public void updatePromoCode(String id, String code, String description,
                                String discountType, BigDecimal discountValue,
                                BigDecimal minOrderAmount, int maxUses,
                                String validUntil, boolean isActive) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("code", code.toUpperCase().trim());
        body.addProperty("discount_type", discountType);
        body.addProperty("discount_value", discountValue);
        if (minOrderAmount != null)
            body.addProperty("min_order_amount", minOrderAmount);
        else
            body.add("min_order_amount", JsonNull.INSTANCE);
        body.addProperty("max_uses", maxUses);
        if (validUntil != null && !validUntil.isBlank())
            body.addProperty("valid_until", validUntil);
        else
            body.add("valid_until", JsonNull.INSTANCE);
        body.addProperty("is_active", isActive);

        String url = SupabaseConfig.REST_URL + "/promo_codes?id=eq." + id;
        client.patch(url, gson.toJson(body));
    }

    public void deletePromoCode(String id) throws Exception {
        String url = SupabaseConfig.REST_URL + "/promo_codes?id=eq." + id;
        client.delete(url);
    }

    public void toggleActive(String id, boolean active) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("is_active", active);
        String url = SupabaseConfig.REST_URL + "/promo_codes?id=eq." + id;
        client.patch(url, gson.toJson(body));
    }
}
