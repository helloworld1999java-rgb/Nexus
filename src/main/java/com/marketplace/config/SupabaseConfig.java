package com.marketplace.config;

public class SupabaseConfig {
    public static final String SUPABASE_URL = "https://ymfwfemuicurjndccqkp.supabase.co";
    public static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InltZndmZW11aWN1cmpuZGNjcWtwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzcyNzkyMjgsImV4cCI6MjA5Mjg1NTIyOH0.4mgTg_CqpJtBoYKOzU-3mL_WGIsalek3ivcwJuZ7hag";
    public static final String SUPABASE_SERVICE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InltZndmZW11aWN1cmpuZGNjcWtwIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc3NzI3OTIyOCwiZXhwIjoyMDkyODU1MjI4fQ.Xbbx-SGSku_J_AmzxjpTetnzsU7gaJn7m23YwMMm7Sk";

    public static final String REST_URL = SUPABASE_URL + "/rest/v1";
    public static final String AUTH_URL = SUPABASE_URL + "/auth/v1";
    public static final String STORAGE_URL = SUPABASE_URL + "/storage/v1";

    public static final String PRODUCTS_BUCKET = "product-images";
}
