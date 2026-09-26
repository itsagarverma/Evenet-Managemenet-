package com.sagar.eventmanagement.gallery.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.gallery.storage.supabase")
public class SupabaseStorageProperties {
    private String endpoint;
    private String region;
    private String accessKey;
    private String secretKey;
    private String bucket = "gallery";
    private String publicUrl;

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getAccessKey() { return accessKey; }
    public void setAccessKey(String accessKey) { this.accessKey = accessKey; }
    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
    public String getBucket() { return bucket; }
    public void setBucket(String bucket) { this.bucket = bucket; }
    public String getPublicUrl() { return publicUrl; }
    public void setPublicUrl(String publicUrl) { this.publicUrl = publicUrl; }

    public void validate() {
        if (blank(endpoint) || blank(region) || blank(accessKey) || blank(secretKey) || blank(bucket) || blank(publicUrl)) {
            throw new IllegalStateException("Supabase Storage configuration is incomplete; configure endpoint, region, access key, secret key, bucket, and public URL");
        }
        if (!endpoint.startsWith("https://") || !publicUrl.startsWith("https://")) {
            throw new IllegalStateException("Supabase Storage endpoints must use HTTPS");
        }
        if (!bucket.matches("[a-z0-9][a-z0-9_-]{1,62}")) {
            throw new IllegalStateException("Supabase Storage bucket name is invalid");
        }
    }

    private boolean blank(String value) { return value == null || value.isBlank(); }
}
