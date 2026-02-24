package com.fintech.backend.controller;

import com.fintech.backend.model.Startup;
import com.fintech.backend.model.Startup.MetricsSnapshot;
import com.fintech.backend.model.StartupMetric;
import com.fintech.backend.repository.InvestmentRepository;
import com.fintech.backend.repository.StartupMetricsRepository;
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
    private final InvestmentRepository investmentRepo;
    private final StartupMetricsRepository metricsRepo;

    // Обратите внимание: добавлен метрик-репозиторий в конструктор
    public StartupsController(StartupsRepository repo,
                              InvestmentRepository investmentRepo,
                              StartupMetricsRepository metricsRepo) {
        this.repo = repo;
        this.investmentRepo = investmentRepo;
        this.metricsRepo = metricsRepo;
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
            // 1. Проверка владельца (простейшая для MVP)
            if (req.founderId != null && !req.founderId.equals(s.getFounderId())) {
                return ResponseEntity.status(403).body(Map.of("error", "not owner"));
            }

            // 2. Проверка: есть ли инвестиции
            boolean hasInvestments = !investmentRepo.findByStartupId(s.getId()).isEmpty();

            // --- ОБЫЧНЫЕ ПОЛЯ ---
            if (req.name != null) s.setName(req.name);
            if (req.stage != null) s.setStage(req.stage);
            if (req.industry != null) s.setIndustry(req.industry);
            if (req.shortPitch != null) s.setShortPitch(req.shortPitch);
            if (req.description != null) s.setDescription(req.description);
            if (req.website != null) s.setWebsite(req.website);
            if (req.logoUrl != null) s.setLogoUrl(req.logoUrl);
            if (req.attachments != null) s.setAttachments(req.attachments);
            if (req.visibility != null) s.setVisibility(req.visibility);

            // --- ВАЖНАЯ ЛОГИКА (valuation + сохранение метрик в отдельную коллекцию) ---
            if (req.metricsSnapshot != null) {
                Double newPre = req.metricsSnapshot.getValuationPreMoney();
                Double newPost = req.metricsSnapshot.getValuationPostMoney();

                // ❗ если уже есть инвестиции → запрещаем менять valuation-поля
                if (hasInvestments && (newPre != null || newPost != null)) {
                    return ResponseEntity.badRequest().body(
                            Map.of("error", "cannot change valuation after investments")
                    );
                }

                // Обеспечим, что у стартапа есть metricsSnapshot объект
                MetricsSnapshot ms = s.getMetricsSnapshot();

                if (ms == null) {
                    ms = new MetricsSnapshot();
                    s.setMetricsSnapshot(ms);
                }

                // Переписываем простые метрики если пришли
                if (req.metricsSnapshot.getMrr() != null) ms.setMrr(req.metricsSnapshot.getMrr());
                if (req.metricsSnapshot.getActiveUsers() != null) ms.setActiveUsers(req.metricsSnapshot.getActiveUsers());
                if (req.metricsSnapshot.getBurnRate() != null) {
                    ms.setBurnRate(req.metricsSnapshot.getBurnRate());
                }
                if (req.metricsSnapshot.getOther() != null) ms.setOther(req.metricsSnapshot.getOther());

                // логика пересчёта valuation (используем текущий режим оценки стартапа)
                String effectiveMode = (req.valuationMode != null) ? req.valuationMode : s.getValuationMode();
                if ("pre".equals(effectiveMode) && newPre != null) {
                    ms.setValuationPreMoney(newPre);
                    // post можно оставить как есть или рассчитывать, пока оставляем без изменений
                } else if ("post".equals(effectiveMode) && newPost != null) {
                    ms.setValuationPostMoney(newPost);
                }

                // Сохраняем отдельную запись в коллекции startup_metrics — snapshot истории
                try {
                    StartupMetric metric = new StartupMetric();
                    metric.setStartupId(s.getId());
                    metric.setDate(Instant.now());
                    metric.setMrr(ms.getMrr());
                    metric.setActiveUsers(ms.getActiveUsers());
                    metric.setBurnRate(ms.getBurnRate());
                    metric.setValuationPreMoney(ms.getValuationPreMoney());
                    metric.setValuationPostMoney(ms.getValuationPostMoney());
                    metric.setOther(ms.getOther());
                    metric.setCreatedAt(Instant.now());
                    metric.setUpdatedAt(metric.getCreatedAt());
                    metricsRepo.save(metric);
                } catch (Exception ex) {
                    // Не критично — логировать по желанию, но не мешаем основному сохранению.
                    // Можно вернуть ошибку, если хотите строгую консистентность.
                    System.err.println("Failed to save startup metric: " + ex.getMessage());
                }
            }

            // режим оценки
            if (req.valuationMode != null) {
                if (hasInvestments) {
                    return ResponseEntity.badRequest().body(
                            Map.of("error", "cannot change valuation mode after investments")
                    );
                }
                s.setValuationMode(req.valuationMode);
            }

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