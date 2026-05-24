package com.marketplace.service;

import com.google.gson.*;
import com.marketplace.config.AppState;
import com.marketplace.config.SupabaseConfig;
import com.marketplace.model.User;

public class AuthService {
    private static final Gson gson = new Gson();
    private final SupabaseClient client = SupabaseClient.getInstance();

    public User signUp(String email, String password, String fullName) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("email", email);
        body.addProperty("password", password);
        JsonObject meta = new JsonObject();
        meta.addProperty("full_name", fullName);
        body.add("data", meta);

        String response = client.postAuth(SupabaseConfig.AUTH_URL + "/signup", gson.toJson(body));
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();

        if (json.has("error")) throw new Exception(json.get("error").getAsString());

        String userId = json.getAsJsonObject("user").get("id").getAsString();
        String accessToken = json.has("access_token") ? json.get("access_token").getAsString() : null;
        String refreshToken = json.has("refresh_token") ? json.get("refresh_token").getAsString() : null;

        AppState.getInstance().setAccessToken(accessToken);
        AppState.getInstance().setRefreshToken(refreshToken);

        JsonObject profile = new JsonObject();
        profile.addProperty("id", userId);
        profile.addProperty("email", email);
        profile.addProperty("full_name", fullName);
        profile.addProperty("role", "user");

        try {
            client.post(SupabaseConfig.REST_URL + "/profiles", gson.toJson(profile));
        } catch (Exception ignored) {}

        return getUserById(userId);
    }

    public User signIn(String email, String password) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("email", email);
        body.addProperty("password", password);

        String response;
        try {
            response = client.postAuth(SupabaseConfig.AUTH_URL + "/token?grant_type=password",
                    gson.toJson(body));
        } catch (Exception e) {
            throw e;
        }
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();

        if (json.has("error_description")) {
            String desc = json.get("error_description").getAsString();
            if (desc.toLowerCase().contains("invalid login credentials")
                    || desc.toLowerCase().contains("invalid email or password")) {
                throw new Exception("Invalid login credentials");
            }
            throw new Exception(desc);
        }
        if (json.has("error")) throw new Exception(json.get("error").getAsString());

        AppState.getInstance().setAccessToken(json.get("access_token").getAsString());
        AppState.getInstance().setRefreshToken(json.get("refresh_token").getAsString());

        String userId = json.getAsJsonObject("user").get("id").getAsString();
        User user = getUserById(userId);

        if (user != null && user.isBlocked()) {
            AppState.getInstance().logout();
            throw new Exception("Ваш аккаунт заблокирован администратором.");
        }

        AppState.getInstance().setCurrentUser(user);
        return user;
    }

    public void signOut() throws Exception {
        try {
            client.post(SupabaseConfig.AUTH_URL + "/logout", "{}");
        } finally {
            AppState.getInstance().logout();
        }
    }

    public User getUserById(String userId) throws Exception {
        String url = SupabaseConfig.REST_URL + "/profiles?id=eq." + userId + "&select=*";
        String response = client.get(url);
        JsonArray arr = JsonParser.parseString(response).getAsJsonArray();
        if (arr.size() == 0) return null;
        return gson.fromJson(arr.get(0), User.class);
    }

    public void updateUserProfile(String userId, String fullName, String phone) throws Exception {
        JsonObject body = new JsonObject();
        if (fullName != null) body.addProperty("full_name", fullName);
        if (phone != null) body.addProperty("phone", phone);
        String url = SupabaseConfig.REST_URL + "/profiles?id=eq." + userId;
        client.patch(url, gson.toJson(body));
    }

    public void updateEmail(String newEmail) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("email", newEmail);
        client.patch(SupabaseConfig.AUTH_URL + "/user", gson.toJson(body));
    }

    public void updatePassword(String newPassword) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("password", newPassword);
        client.patch(SupabaseConfig.AUTH_URL + "/user", gson.toJson(body));
    }
}
