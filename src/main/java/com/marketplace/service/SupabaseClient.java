package com.marketplace.service;

import com.marketplace.config.AppState;
import com.marketplace.config.SupabaseConfig;
import okhttp3.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class SupabaseClient {
    private static final Logger LOG = Logger.getLogger(SupabaseClient.class.getName());

    private static SupabaseClient instance;
    private final OkHttpClient httpClient;
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private SupabaseClient() {
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(8, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .connectionPool(new okhttp3.ConnectionPool(5, 30, TimeUnit.SECONDS))
                .build();
    }

    public static SupabaseClient getInstance() {
        if (instance == null) instance = new SupabaseClient();
        return instance;
    }

    public Request.Builder baseRequest(String url) {
        return new Request.Builder()
                .url(url)
                .header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                .header("Content-Type", "application/json");
    }

    public Request.Builder authRequest(String url) {
        Request.Builder builder = baseRequest(url);
        String token = AppState.getInstance().getAccessToken();
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        return builder;
    }

    public Request.Builder serviceRequest(String url) {
        return new Request.Builder()
                .url(url)
                .header("apikey", SupabaseConfig.SUPABASE_SERVICE_KEY)
                .header("Authorization", "Bearer " + SupabaseConfig.SUPABASE_SERVICE_KEY)
                .header("Content-Type", "application/json");
    }

    public Response execute(Request request) throws IOException {
        return httpClient.newCall(request).execute();
    }

    public String get(String url) throws IOException {
        LOG.info("[GET] " + url);
        Request request = authRequest(url).get().build();
        try (Response response = execute(request)) {
            String body = response.body() != null ? response.body().string() : "";
            LOG.info("[GET] HTTP " + response.code() + " <- " + truncate(body));
            if (!response.isSuccessful()) throw new IOException("HTTP " + response.code() + ": " + body);
            return body;
        }
    }

    public String post(String url, String json) throws IOException {
        LOG.info("[POST] " + url);
        LOG.info("[POST] Body: " + truncate(json));
        RequestBody body = RequestBody.create(json, JSON);
        Request request = authRequest(url)
                .post(body)
                .header("Prefer", "return=representation")
                .build();
        try (Response response = execute(request)) {
            String respBody = response.body() != null ? response.body().string() : "";
            LOG.info("[POST] HTTP " + response.code() + " <- " + truncate(respBody));
            if (!response.isSuccessful()) throw new IOException("HTTP " + response.code() + ": " + respBody);
            return respBody;
        }
    }

    public String postAuth(String url, String json) throws IOException {
        LOG.info("[POST-AUTH] " + url);
        RequestBody body = RequestBody.create(json, JSON);
        Request request = baseRequest(url).post(body).build();
        try (Response response = execute(request)) {
            String respBody = response.body() != null ? response.body().string() : "";
            LOG.info("[POST-AUTH] HTTP " + response.code() + " <- " + truncate(respBody));
            if (!response.isSuccessful()) throw new IOException("HTTP " + response.code() + ": " + respBody);
            return respBody;
        }
    }

    public String patch(String url, String json) throws IOException {
        LOG.info("[PATCH] " + url);
        LOG.info("[PATCH] Body: " + truncate(json));
        RequestBody body = RequestBody.create(json, JSON);
        Request request = authRequest(url)
                .patch(body)
                .header("Prefer", "return=representation")
                .build();
        try (Response response = execute(request)) {
            String respBody = response.body() != null ? response.body().string() : "";
            LOG.info("[PATCH] HTTP " + response.code() + " <- " + truncate(respBody));
            if (!response.isSuccessful()) throw new IOException("HTTP " + response.code() + ": " + respBody);
            return respBody;
        }
    }

    public void delete(String url) throws IOException {
        LOG.info("[DELETE] " + url);
        Request request = authRequest(url).delete().build();
        try (Response response = execute(request)) {
            String body = response.body() != null ? response.body().string() : "";
            LOG.info("[DELETE] HTTP " + response.code() + " <- " + truncate(body));
            if (!response.isSuccessful()) throw new IOException("HTTP " + response.code() + ": " + body);
        }
    }

    public String postService(String url, String json) throws IOException {
        LOG.info("[POST-SERVICE] " + url);
        RequestBody body = RequestBody.create(json, JSON);
        Request request = serviceRequest(url)
                .post(body)
                .header("Prefer", "return=representation")
                .build();
        try (Response response = execute(request)) {
            String respBody = response.body() != null ? response.body().string() : "";
            LOG.info("[POST-SERVICE] HTTP " + response.code() + " <- " + truncate(respBody));
            if (!response.isSuccessful()) throw new IOException("HTTP " + response.code() + ": " + respBody);
            return respBody;
        }
    }

    public String patchService(String url, String json) throws IOException {
        LOG.info("[PATCH-SERVICE] " + url);
        RequestBody body = RequestBody.create(json, JSON);
        Request request = serviceRequest(url)
                .patch(body)
                .header("Prefer", "return=representation")
                .build();
        try (Response response = execute(request)) {
            String respBody = response.body() != null ? response.body().string() : "";
            LOG.info("[PATCH-SERVICE] HTTP " + response.code() + " <- " + truncate(respBody));
            if (!response.isSuccessful()) throw new IOException("HTTP " + response.code() + ": " + respBody);
            return respBody;
        }
    }

    public String getService(String url) throws IOException {
        LOG.info("[GET-SERVICE] " + url);
        Request request = serviceRequest(url).get().build();
        try (Response response = execute(request)) {
            String body = response.body() != null ? response.body().string() : "";
            LOG.info("[GET-SERVICE] HTTP " + response.code() + " <- " + truncate(body));
            if (!response.isSuccessful()) throw new IOException("HTTP " + response.code() + ": " + body);
            return body;
        }
    }

    public OkHttpClient getHttpClient() { return httpClient; }

    private String truncate(String s) {
        if (s == null) return "null";
        return s.length() > 500 ? s.substring(0, 500) + "..." : s;
    }
}