package com.fintech.backend.controller;

import com.fintech.backend.model.Startup;
import com.fintech.backend.model.Startup.MetricsSnapshot;
import com.fintech.backend.repository.StartupsRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/startups")
@CrossOrigin(origins = "http://localhost:5173")
public class StartupsController {

    private final StartupsRepository repo;

    public StartupsController(StartupsRepository repo) {
        this.repo = repo;
    }

    // DTO для создания/обновления
    public static class StartupRequest {
        public String name;
        public String slug;
        public String founderId;
        public String stage;
        public String industry;
        public String shortPitch;
        public String description;
        public String website;
        public String logoUrl;
        public MetricsSnapshot metricsSnapshot;
        public List<String> attachments;
        public String visibility;
        public String valuationMode;
    }

    // Утилита: генерируем slug из name (simple)
    private String makeSlug(String name) {
        if (name == null) return null;
        String nowhitespace = name.trim().toLowerCase();
        // нормализуем (удаляем акценты) и заменяем всё неалфанум на '-'
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String slug = normalized.replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        if (slug.isEmpty()) slug = "startup-" + UUID.randomUUID().toString().substring(0, 8);
        return slug;
    }

    // GET /api/startups?stage=seed&industry=FinTech&q=pay
    @GetMapping
    public ResponseEntity<List<Startup>> list(
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) String q
    ) {
        List<Startup> base;
        if (stage != null && industry != null) base = repo.findAllByStageAndIndustry(stage, industry);
        else if (stage != null) base = repo.findAllByStage(stage);
        else base = repo.findAll();

        // простой текстовый фильтр по name / shortPitch / description
        if (q != null && !q.isBlank()) {
            String ql = q.toLowerCase();
            base = base.stream().filter(s ->
                    (s.getName() != null && s.getName().toLowerCase().contains(ql)) ||
                            (s.getShortPitch() != null && s.getShortPitch().toLowerCase().contains(ql)) ||
                            (s.getDescription() != null && s.getDescription().toLowerCase().contains(ql))
            ).collect(Collectors.toList());
        }

        // скрываем ничего — фронту нужен весь объект (если нужно, можно очищать поля)
        return ResponseEntity.ok(base);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody StartupRequest req) {
        if (req == null || req.name == null || req.name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "name required"));
        }

        String slug = (req.slug == null || req.slug.isBlank()) ? makeSlug(req.name) : req.slug.trim().toLowerCase();
        // если slug занят, добавим суффикс
        String baseSlug = slug;
        int suffix = 0;
        while (repo.existsBySlug(slug)) {
            suffix++;
            slug = baseSlug + "-" + suffix;
        }

        Startup s = new Startup();
        s.setName(req.name);
        s.setSlug(slug);
        s.setFounderId(req.founderId);
        s.setStage(req.stage == null ? "idea" : req.stage);
        s.setIndustry(req.industry);
        s.setShortPitch(req.shortPitch);
        s.setDescription(req.description);
        s.setWebsite(req.website);
        s.setLogoUrl(req.logoUrl);
        s.setMetricsSnapshot(req.metricsSnapshot == null ? new MetricsSnapshot() : req.metricsSnapshot);
        s.setAttachments(req.attachments);
        s.setVisibility(req.visibility == null ? "public" : req.visibility);
        s.setCreatedAt(Instant.now());
        s.setUpdatedAt(s.getCreatedAt());
        s.setValuationMode(
                req.valuationMode == null ? "pre" : req.valuationMode
        );

        Startup saved = repo.save(s);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody StartupRequest req) {
        return repo.findById(id).map(s -> {
            if (req.name != null) s.setName(req.name);
            if (req.slug != null && !req.slug.isBlank()) {
                String newSlug = req.slug.trim().toLowerCase();
                if (!newSlug.equals(s.getSlug())) {
                    String baseSlug = newSlug;
                    int suffix = 0;
                    while (repo.existsBySlug(newSlug)) {
                        suffix++;
                        newSlug = baseSlug + "-" + suffix;
                    }
                    s.setSlug(newSlug);
                }
            }
            if (req.stage != null) s.setStage(req.stage);
            if (req.industry != null) s.setIndustry(req.industry);
            if (req.shortPitch != null) s.setShortPitch(req.shortPitch);
            if (req.description != null) s.setDescription(req.description);
            if (req.website != null) s.setWebsite(req.website);
            if (req.logoUrl != null) s.setLogoUrl(req.logoUrl);
            if (req.metricsSnapshot != null) s.setMetricsSnapshot(req.metricsSnapshot);
            if (req.attachments != null) s.setAttachments(req.attachments);
            if (req.visibility != null) s.setVisibility(req.visibility);
            if (req.valuationMode != null)
                s.setValuationMode(req.valuationMode);

            s.setUpdatedAt(Instant.now());
            Startup saved = repo.save(s);
            return ResponseEntity.ok(saved);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
