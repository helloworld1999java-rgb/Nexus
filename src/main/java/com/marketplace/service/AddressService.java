package com.marketplace.service;

import com.google.gson.*;
import com.marketplace.config.AppState;
import com.marketplace.config.SupabaseConfig;
import com.marketplace.model.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AddressService {
    private static final Logger log = Logger.getLogger(AddressService.class.getName());
    private static final Gson gson = new Gson();
    private final SupabaseClient client = SupabaseClient.getInstance();

    // S1192: константа вместо дублирующегося литерала "/addresses?id=eq."
    private static final String ADDRESS_BY_ID = "/addresses?id=eq.";

    public List<Address> getUserAddresses() throws IOException {
        String userId = AppState.getInstance().getCurrentUser().getId();
        String url = SupabaseConfig.REST_URL + "/addresses?user_id=eq." + userId
                + "&select=*&order=is_default.desc";
        log.log(Level.INFO, "[Address] GET {0}", url);
        String response = client.get(url);
        log.log(Level.INFO, "[Address] GET response: {0}", response);
        JsonArray arr = JsonParser.parseString(response).getAsJsonArray();
        List<Address> addresses = new ArrayList<>();
        for (JsonElement el : arr) addresses.add(gson.fromJson(el, Address.class));
        return addresses;
    }

    public Address createAddress(Address address) throws IOException {
        String userId = AppState.getInstance().getCurrentUser().getId();
        address.setUserId(userId);

        String json = gson.toJson(address);
        String url  = SupabaseConfig.REST_URL + "/addresses?select=*";
        log.log(Level.INFO, "[Address] POST {0}", url);
        log.log(Level.INFO, "[Address] POST body: {0}", json);

        String response = client.post(url, json);
        log.log(Level.INFO, "[Address] POST response: {0}", response);

        JsonArray arr = JsonParser.parseString(response).getAsJsonArray();
        if (arr.isEmpty())
            throw new IOException("Сервер вернул пустой ответ при создании адреса");
        return gson.fromJson(arr.get(0), Address.class);
    }

    public void updateAddress(String id, Address address) throws IOException {
        String json = gson.toJson(address);
        String url  = SupabaseConfig.REST_URL + ADDRESS_BY_ID + id;
        log.log(Level.INFO, "[Address] PATCH {0}", url);
        log.log(Level.INFO, "[Address] PATCH body: {0}", json);
        String response = client.patch(url, json);
        log.log(Level.INFO, "[Address] PATCH response: {0}", response);
    }

    public void deleteAddress(String id) throws IOException {
        String url = SupabaseConfig.REST_URL + ADDRESS_BY_ID + id;
        log.log(Level.INFO, "[Address] DELETE {0}", url);
        client.delete(url);
    }

    public void setDefault(String id) throws IOException {
        String userId = AppState.getInstance().getCurrentUser().getId();
        log.log(Level.INFO, "[Address] setDefault id={0}", id);
        JsonObject off = new JsonObject();
        off.addProperty("is_default", false);
        client.patch(SupabaseConfig.REST_URL + "/addresses?user_id=eq." + userId, gson.toJson(off));
        JsonObject on = new JsonObject();
        on.addProperty("is_default", true);
        client.patch(SupabaseConfig.REST_URL + ADDRESS_BY_ID + id, gson.toJson(on));
    }
}