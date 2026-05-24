package com.marketplace.service;

import com.google.gson.*;
import com.marketplace.config.SupabaseConfig;
import com.marketplace.model.PromoCode;
import java.math.BigDecimal;

public class PromoCodeService {
    private static final Gson gson = new Gson();
    private final SupabaseClient client = SupabaseClient.getInstance();

    public PromoCode validatePromoCode(String code, BigDecimal orderAmount) throws Exception {
        String url = SupabaseConfig.REST_URL + "/promo_codes?code=eq."
                + code.toUpperCase() + "&is_active=eq.true&select=*";
        String response = client.get(url);
        JsonArray arr = JsonParser.parseString(response).getAsJsonArray();
        if (arr.size() == 0) throw new Exception("Промокод не найден или неактивен");

        PromoCode promo = gson.fromJson(arr.get(0), PromoCode.class);

        if (promo.getMaxUses() > 0 && promo.getUsedCount() >= promo.getMaxUses()) {
            throw new Exception("Промокод исчерпан");
        }
        if (promo.getMinOrderAmount() != null && orderAmount.compareTo(promo.getMinOrderAmount()) < 0) {
            throw new Exception("Минимальная сумма заказа: " + promo.getMinOrderAmount() + " ₽");
        }
        if (promo.getValidUntil() != null) {
            java.time.LocalDate validUntil = java.time.LocalDate.parse(promo.getValidUntil().substring(0, 10));
            if (java.time.LocalDate.now().isAfter(validUntil)) throw new Exception("Срок действия промокода истёк");
        }
        return promo;
    }

    public void incrementUsage(String promoId) throws Exception {
        String url = SupabaseConfig.REST_URL + "/promo_codes?id=eq." + promoId + "&select=used_count";
        String response = client.get(url);
        JsonArray arr = JsonParser.parseString(response).getAsJsonArray();
        if (arr.size() == 0) return;
        int current = arr.get(0).getAsJsonObject().get("used_count").getAsInt();
        JsonObject body = new JsonObject();
        body.addProperty("used_count", current + 1);
        client.patch(SupabaseConfig.REST_URL + "/promo_codes?id=eq." + promoId, gson.toJson(body));
    }
}
