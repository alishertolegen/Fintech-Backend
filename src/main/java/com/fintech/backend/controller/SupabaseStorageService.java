// SupabaseStorageService.java
package com.fintech.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Service
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.bucket}")
    private String bucket;

    private final RestTemplate rest = new RestTemplate();

    public String uploadAvatar(MultipartFile file, String userId) throws Exception {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("File is empty");

        // sanitize filename
        String original = file.getOriginalFilename() == null ? "avatar" : file.getOriginalFilename();
        String sanitized = original.replaceAll("[^A-Za-z0-9._-]", "_");

        final String filename = sanitized; // ← теперь final

        String path = String.format("avatars/%s/%d-%s",
                userId,
                Instant.now().getEpochSecond(),
                URLEncoder.encode(filename, StandardCharsets.UTF_8.toString())
        );

        ByteArrayResource body = new ByteArrayResource(file.getBytes()) {
            @Override public String getFilename() { return filename; }
        };
        String url = String.format("%s/storage/v1/object/%s/%s", supabaseUrl, bucket, path);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.set("apikey", supabaseKey);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        HttpEntity<ByteArrayResource> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> resp = rest.exchange(url, HttpMethod.POST, entity, String.class);

        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Supabase upload failed: " + resp.getStatusCode() + " / " + resp.getBody());
        }

        // Если бакет публичный — конструируем публичный URL
        String publicUrl = String.format("%s/storage/v1/object/public/%s/%s", supabaseUrl, bucket, path);
        return publicUrl;
    }
}