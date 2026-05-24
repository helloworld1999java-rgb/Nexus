package com.marketplace.service;

import com.google.gson.*;
import com.marketplace.config.AppState;
import com.marketplace.config.SupabaseConfig;
import com.marketplace.model.PaymentCard;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class PaymentCardService {
    private static final Logger log = Logger.getLogger(PaymentCardService.class.getName());
    private static final Gson gson = new Gson();
    private final SupabaseClient client = SupabaseClient.getInstance();

    public List<PaymentCard> getUserCards() throws Exception {
        String userId = AppState.getInstance().getCurrentUser().getId();
        String url = SupabaseConfig.REST_URL + "/payment_cards?user_id=eq." + userId
                + "&select=*&order=is_default.desc,created_at.desc";
        log.info("[Cards] GET " + url);
        String response = client.get(url);
        log.info("[Cards] GET response: " + response);
        JsonArray arr = JsonParser.parseString(response).getAsJsonArray();
        List<PaymentCard> cards = new ArrayList<>();
        for (JsonElement el : arr) cards.add(gson.fromJson(el, PaymentCard.class));
        return cards;
    }

    public PaymentCard addCard(String cardNumber, String cardHolder, int month, int year) throws Exception {
        String userId = AppState.getInstance().getCurrentUser().getId();
        String digits = cardNumber.replaceAll("\\s", "");
        String masked = digits.substring(digits.length() - 4);
        String type   = detectCardType(digits);

        JsonObject body = new JsonObject();
        body.addProperty("user_id",         userId);
        body.addProperty("card_number_mask", masked);
        body.addProperty("card_holder",      cardHolder);
        body.addProperty("expiry_month",     month);
        body.addProperty("expiry_year",      year);
        body.addProperty("card_type",        type);
        body.addProperty("is_default",       false);

        String json = gson.toJson(body);
        String url  = SupabaseConfig.REST_URL + "/payment_cards?select=*";
        log.info("[Cards] POST " + url);
        log.info("[Cards] POST body: " + json);

        String response = client.post(url, json);
        log.info("[Cards] POST response: " + response);

        JsonArray arr = JsonParser.parseString(response).getAsJsonArray();
        if (arr.size() == 0) throw new Exception("Сервер вернул пустой ответ при добавлении карты");
        return gson.fromJson(arr.get(0), PaymentCard.class);
    }

    public void deleteCard(String id) throws Exception {
        String url = SupabaseConfig.REST_URL + "/payment_cards?id=eq." + id;
        log.info("[Cards] DELETE " + url);
        client.delete(url);
    }

    public void setDefault(String id) throws Exception {
        String userId = AppState.getInstance().getCurrentUser().getId();
        log.info("[Cards] setDefault id=" + id);
        JsonObject off = new JsonObject(); off.addProperty("is_default", false);
        client.patch(SupabaseConfig.REST_URL + "/payment_cards?user_id=eq." + userId, gson.toJson(off));
        JsonObject on  = new JsonObject(); on.addProperty("is_default", true);
        client.patch(SupabaseConfig.REST_URL + "/payment_cards?id=eq." + id, gson.toJson(on));
    }

    private String detectCardType(String number) {
        if (number.startsWith("4"))                                      return "visa";
        if (number.startsWith("2200") || number.startsWith("2201")
                || number.startsWith("2202") || number.startsWith("2203")
                || number.startsWith("2204"))                            return "mir";
        if (number.startsWith("34") || number.startsWith("37"))         return "amex";
        if (number.startsWith("5") || number.startsWith("2"))           return "mastercard";
        return "other";
    }
}