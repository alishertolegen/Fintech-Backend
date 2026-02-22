package com.fintech.backend.controller;

import com.fintech.backend.model.StartupMetric;
import com.fintech.backend.repository.StartupMetricsRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/startup-metrics")
@CrossOrigin(origins = "http://localhost:5173")
public class StartupMetricsController {

    private final StartupMetricsRepository repo;

    public StartupMetricsController(StartupMetricsRepository repo) {
        this.repo = repo;
    }

    // Create
    @PostMapping
    public ResponseEntity<?> create(@RequestBody StartupMetric req) {
        if (req == null || req.getStartupId() == null || req.getDate() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "startupId and date required"));
        }
        req.setCreatedAt(Instant.now());
        req.setUpdatedAt(req.getCreatedAt());
        StartupMetric saved = repo.save(req);
        return ResponseEntity.ok(saved);
    }

    // Read: list (optionally by startupId and date range)
    // /api/startup-metrics?startupId=...&from=2025-09-01T00:00:00Z&to=2025-10-01T00:00:00Z
    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(required = false) String startupId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        if (startupId == null || startupId.isBlank()) {
            // возвращаем все метрики (осторожно — может быть много)
            List<StartupMetric> all = repo.findAll();
            return ResponseEntity.ok(all);
        }

        // если указан диапазон дат — используем его
        if (from != null || to != null) {
            try {
                Instant fromI = (from == null) ? Instant.EPOCH : Instant.parse(from);
                Instant toI = (to == null) ? Instant.now() : Instant.parse(to);
                List<StartupMetric> list = repo.findAllByStartupIdAndDateBetweenOrderByDateDesc(startupId, fromI, toI);
                return ResponseEntity.ok(list);
            } catch (DateTimeParseException ex) {
                return ResponseEntity.badRequest().body(Map.of("error", "invalid date format, use ISO-8601 (e.g. 2025-10-01T00:00:00Z)"));
            }
        }

        List<StartupMetric> list = repo.findAllByStartupIdOrderByDateDesc(startupId);
        return ResponseEntity.ok(list);
    }

    // Get by id
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        Optional<StartupMetric> op = repo.findById(id);
        return op.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Update
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody StartupMetric req) {
        return repo.findById(id).map(existing -> {
            if (req.getDate() != null) existing.setDate(req.getDate());
            if (req.getMrr() != null) existing.setMrr(req.getMrr());
            if (req.getActiveUsers() != null) existing.setActiveUsers(req.getActiveUsers());
            if (req.getBurnRate() != null) existing.setBurnRate(req.getBurnRate());

            // поддержка новых полей
            if (req.getValuationPreMoney() != null) existing.setValuationPreMoney(req.getValuationPreMoney());
            if (req.getValuationPostMoney() != null) existing.setValuationPostMoney(req.getValuationPostMoney());

            if (req.getOther() != null) existing.setOther(req.getOther());
            existing.setUpdatedAt(Instant.now());
            StartupMetric saved = repo.save(existing);
            return ResponseEntity.ok(saved);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
