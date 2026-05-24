package com.marketplace.service;

import com.google.gson.*;
import com.marketplace.config.SupabaseConfig;
import com.marketplace.model.Product;
import java.util.*;

public class ProductService {
    private static final Gson gson = new Gson();
    private final SupabaseClient client = SupabaseClient.getInstance();

    private final Map<String, String> categoryIdCache = new HashMap<>();

    public List<Product> getProducts(String category, String search, String sortBy, String sortOrder,
                                     Double minPrice, Double maxPrice, Double minRating,
                                     int limit, int offset) throws Exception {
        StringBuilder url = new StringBuilder(SupabaseConfig.REST_URL +
                "/products?select=*&is_active=eq.true");

        if (category != null && !category.isEmpty()) {
            String catId = getCategoryId(category);
            if (catId != null) {
                url.append("&category_id=eq.").append(catId);
            }
        }
        if (search != null && !search.isEmpty()) {
            String encoded = encode(search);
            if (search.matches("[A-Za-z]{2}-\\d{4,8}")) {
                url.append("&article=eq.").append(search.toUpperCase());
            } else {
                url.append("&name=ilike.*").append(encoded).append("*");
            }
        }
        if (minPrice != null) url.append("&price=gte.").append(minPrice);
        if (maxPrice != null) url.append("&price=lte.").append(maxPrice);
        if (minRating != null) url.append("&rating=gte.").append(minRating);

        String actualSort = mapSortColumn(sortBy);
        if (actualSort != null) {
            url.append("&order=").append(actualSort).append(".");
            url.append(sortOrder != null ? sortOrder : "asc");
        } else {
            url.append("&order=created_at.desc");
        }

        url.append("&limit=").append(limit).append("&offset=").append(offset);

        String response = client.get(url.toString());
        return parseProducts(response);
    }

    public List<Product> getFeaturedProducts() throws Exception {
        String url = SupabaseConfig.REST_URL +
                "/products?select=*&is_active=eq.true&order=rating.desc&limit=10";
        String response = client.get(url);
        return parseProducts(response);
    }

    public Product getProductById(String id) throws Exception {
        String url = SupabaseConfig.REST_URL + "/products?id=eq." + id + "&select=*";
        String response = client.get(url);
        JsonArray arr = JsonParser.parseString(response).getAsJsonArray();
        if (arr.size() == 0) throw new Exception("Товар не найден");
        return gson.fromJson(arr.get(0), Product.class);
    }

    public List<Product> getSellerProducts(String sellerId) throws Exception {
        String url = SupabaseConfig.REST_URL +
                "/products?seller_id=eq." + sellerId + "&select=*&order=created_at.desc";
        String response = client.get(url);
        return parseProducts(response);
    }

    public Product createProduct(Product product) throws Exception {
        String catId = getCategoryId(product.getCategory());
        if (catId == null) throw new Exception("Категория не найдена: " + product.getCategory());

        String url = SupabaseConfig.REST_URL + "/products";
        JsonObject body = buildProductJson(product, catId);

        String response = client.post(url + "?select=*", body.toString());
        JsonArray arr = JsonParser.parseString(response).getAsJsonArray();
        if (arr.size() == 0) throw new Exception("Ошибка создания товара");
        return gson.fromJson(arr.get(0), Product.class);
    }

    public void updateProduct(String id, Product product) throws Exception {
        String catId = getCategoryId(product.getCategory());
        if (catId == null) throw new Exception("Категория не найдена: " + product.getCategory());

        String url = SupabaseConfig.REST_URL + "/products?id=eq." + id;
        JsonObject body = buildProductJson(product, catId);
        client.patch(url, body.toString());
    }

    private JsonObject buildProductJson(Product product, String categoryUuid) {
        JsonObject body = new JsonObject();
        if (product.getSellerId() != null) body.addProperty("seller_id", product.getSellerId());
        if (product.getName() != null)     body.addProperty("name", product.getName());
        if (product.getDescription() != null) body.addProperty("description", product.getDescription());
        if (product.getPrice() != null)    body.addProperty("price", product.getPrice());
        if (product.getOldPrice() != null) body.addProperty("old_price", product.getOldPrice());
        body.addProperty("category_id", categoryUuid);
        if (product.getBrand() != null && !product.getBrand().isEmpty())
            body.addProperty("brand", product.getBrand());
        body.addProperty("stock", product.getStock());
        if (product.getImageUrls() != null)
            body.add("image_urls", gson.toJsonTree(product.getImageUrls()));
        body.addProperty("is_active", product.isActive());

        if (product.getSupplierCountry() != null && !product.getSupplierCountry().isEmpty())
            body.addProperty("supplier_country", product.getSupplierCountry());
        if (product.getDeliveryDaysMin() != null)
            body.addProperty("delivery_days_min", product.getDeliveryDaysMin());
        if (product.getDeliveryDaysMax() != null)
            body.addProperty("delivery_days_max", product.getDeliveryDaysMax());

        return body;
    }

    public void deleteProduct(String id) throws Exception {
        String url = SupabaseConfig.REST_URL + "/products?id=eq." + id;
        client.delete(url);
    }

    public List<String> getCategories() throws Exception {
        String url = SupabaseConfig.REST_URL + "/categories?select=id,name&order=name.asc";
        String response = client.get(url);
        JsonArray arr = JsonParser.parseString(response).getAsJsonArray();
        List<String> names = new ArrayList<>();
        categoryIdCache.clear();
        for (JsonElement el : arr) {
            JsonObject obj = el.getAsJsonObject();
            String name = obj.get("name").getAsString();
            String id   = obj.get("id").getAsString();
            names.add(name);
            categoryIdCache.put(name, id);
        }
        return names;
    }

    public String getCategoryId(String categoryName) throws Exception {
        if (categoryName == null || categoryName.isEmpty()) return null;
        if (categoryIdCache.containsKey(categoryName)) {
            return categoryIdCache.get(categoryName);
        }
        String url = SupabaseConfig.REST_URL +
                "/categories?name=eq." + encode(categoryName) + "&select=id&limit=1";
        String response = client.get(url);
        JsonArray arr = JsonParser.parseString(response).getAsJsonArray();
        if (arr.size() == 0) return null;
        String id = arr.get(0).getAsJsonObject().get("id").getAsString();
        categoryIdCache.put(categoryName, id);
        return id;
    }

    public void setProductActive(String id, boolean active) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("is_active", active);
        String url = SupabaseConfig.REST_URL + "/products?id=eq." + id;
        client.patch(url, gson.toJson(body));
    }

    public String uploadProductImage(String folderId, byte[] imageData, String fileName, String mimeType) throws Exception {
        String ext = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.')) : ".jpg";
        String safeName = UUID.randomUUID().toString() + ext;
        String path = folderId + "/" + safeName;
        String url = SupabaseConfig.STORAGE_URL + "/object/" + SupabaseConfig.PRODUCTS_BUCKET + "/" + path;

        okhttp3.MediaType mediaType = okhttp3.MediaType.parse(mimeType);
        okhttp3.RequestBody requestBody = okhttp3.RequestBody.create(imageData, mediaType);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(requestBody)
                .header("apikey", SupabaseConfig.SUPABASE_SERVICE_KEY)
                .header("Authorization", "Bearer " + SupabaseConfig.SUPABASE_SERVICE_KEY)
                .header("Content-Type", mimeType)
                .header("x-upsert", "true")
                .build();

        try (okhttp3.Response response = client.getHttpClient().newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String body = response.body() != null ? response.body().string() : "";
                throw new Exception("Upload failed: " + body);
            }
        }

        return SupabaseConfig.SUPABASE_URL + "/storage/v1/object/public/"
                + SupabaseConfig.PRODUCTS_BUCKET + "/" + path;
    }

    public void deleteProductImage(String imageUrl) throws Exception {
        String marker = "/" + SupabaseConfig.PRODUCTS_BUCKET + "/";
        int idx = imageUrl.indexOf(marker);
        if (idx < 0) return;
        String path = imageUrl.substring(idx + marker.length());
        String url = SupabaseConfig.STORAGE_URL + "/object/" + SupabaseConfig.PRODUCTS_BUCKET + "/" + path;

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .delete()
                .header("apikey", SupabaseConfig.SUPABASE_SERVICE_KEY)
                .header("Authorization", "Bearer " + SupabaseConfig.SUPABASE_SERVICE_KEY)
                .build();

        try (okhttp3.Response response = client.getHttpClient().newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String body = response.body() != null ? response.body().string() : "";
                throw new Exception("Delete failed: " + body);
            }
        }
    }

    private List<Product> parseProducts(String response) {
        JsonArray arr = JsonParser.parseString(response).getAsJsonArray();
        List<Product> products = new ArrayList<>();
        for (JsonElement el : arr) products.add(gson.fromJson(el, Product.class));
        return products;
    }

    private String mapSortColumn(String sortBy) {
        if (sortBy == null) return null;
        return switch (sortBy) {
            case "sold" -> "sales_count";
            default     -> sortBy;
        };
    }

    private String encode(String s) {
        try {
            return java.net.URLEncoder.encode(s, "UTF-8");
        } catch (Exception e) {
            return s;
        }
    }
}
