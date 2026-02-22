package com.fintech.backend.controller;

import com.fintech.backend.model.Offer;
import com.fintech.backend.repository.OfferRepository;
import com.fintech.backend.repository.StartupsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fintech.backend.model.Investment;
import com.fintech.backend.repository.InvestmentRepository;
import com.fintech.backend.model.Startup;


@RestController
@RequestMapping("/api/offers")
public class OfferController {
    @Autowired
    private InvestmentRepository investmentRepository;

    private final OfferRepository repository;
    private final StartupsRepository startupsRepository;
    @Autowired
    public OfferController(OfferRepository repository, StartupsRepository startupsRepository) {
        this.repository = repository;
        this.startupsRepository = startupsRepository;
    }


    // Получить все (с простыми фильтрами через query params)
    // /api/offers?startupId=...&investorId=...&status=...&visibility=...
    @GetMapping
    public ResponseEntity<List<Offer>> list(
            @RequestParam(required = false) String startupId,
            @RequestParam(required = false) String investorId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String visibility
    ) {
        if (startupId != null) return ResponseEntity.ok(repository.findByStartupId(startupId));
        if (investorId != null) return ResponseEntity.ok(repository.findByInvestorId(investorId));
        if (status != null) return ResponseEntity.ok(repository.findByStatus(status));
        if (visibility != null) return ResponseEntity.ok(repository.findByVisibility(visibility));
        return ResponseEntity.ok(repository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Offer> getById(@PathVariable String id) {
        Optional<Offer> opt = repository.findById(id);
        return opt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Offer> create(@RequestBody Offer payload) {
        payload.setCreatedAt(Instant.now());
        payload.setUpdatedAt(Instant.now());
        // по умолчанию статус если не передан
        if (payload.getStatus() == null) payload.setStatus("sent");
        Offer saved = repository.save(payload);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Offer> replace(@PathVariable String id, @RequestBody Offer payload) {
        return repository.findById(id).map(existing -> {
            existing.setTitle(payload.getTitle());
            existing.setStartupId(payload.getStartupId());
            existing.setInvestorId(payload.getInvestorId());
            existing.setAmount(payload.getAmount());
            existing.setEquityPercent(payload.getEquityPercent());
            existing.setType(payload.getType());
            existing.setVisibility(payload.getVisibility());
            existing.setStatus(payload.getStatus());
            existing.setAttachments(payload.getAttachments());
            existing.setNote(payload.getNote());
            existing.setUpdatedAt(Instant.now());
            return ResponseEntity.ok(repository.save(existing));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Быстрая смена статуса оффера (например accept/reject/counter).
     * Запрос: PATCH /api/offers/{id}/status
     * Тело: { "status": "accepted", "note": "..." }
     */
    public static class StatusUpdate {
        public String status;
        public String note;
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Offer> updateStatus(@PathVariable String id, @RequestBody StatusUpdate body) {
        return (ResponseEntity<Offer>) repository.findById(id).map(existing -> {
            if (body.status != null) existing.setStatus(body.status);
            if (body.note != null) existing.setNote(body.note);
            existing.setUpdatedAt(Instant.now());
            Offer savedOffer = repository.save(existing);

            if ("accepted".equalsIgnoreCase(body.status)) {
                // 🔐 Проверка: уже есть активная инвестиция?
                boolean hasActive = investmentRepository
                        .existsByStartupIdAndStatus(savedOffer.getStartupId(), "active");

                if (hasActive) {
                    return ResponseEntity.badRequest().body(
                            Map.of("error", "Startup already has an active investment")
                    );
                }
                Optional<Startup> optStartup = startupsRepository.findById(savedOffer.getStartupId());
                if (optStartup.isEmpty()) return ResponseEntity.ok(savedOffer);

                Startup startup = optStartup.get();
                Startup.MetricsSnapshot ms = startup.getMetricsSnapshot();

                if (ms == null) {
                    ms = new Startup.MetricsSnapshot();
                }

                Double investment = savedOffer.getAmount().doubleValue();
                Double pre = ms.getValuationPreMoney();
                Double post = ms.getValuationPostMoney();

                Double equity;
                Double valuationPreMoney;
                Double valuationPostMoney;

                // --------------------------------
                // 🔹 PRE-MONEY МОДЕЛЬ
                // --------------------------------
                if ("pre".equalsIgnoreCase(startup.getValuationMode())) {

                    valuationPreMoney = (pre == null) ? 0.0 : pre;
                    valuationPostMoney = valuationPreMoney + investment;

                    equity = investment / valuationPostMoney;

                }
                // --------------------------------
                // 🔹 POST-MONEY МОДЕЛЬ
                // --------------------------------
                else {

                    valuationPostMoney = (post == null) ? investment : post;
                    valuationPreMoney = valuationPostMoney - investment;

                    equity = investment / valuationPostMoney;
                }

                // Обновляем snapshot
                ms.setValuationPreMoney(valuationPreMoney);
                ms.setValuationPostMoney(valuationPostMoney);
                startup.setMetricsSnapshot(ms);
                startup.setUpdatedAt(Instant.now());
                startupsRepository.save(startup);

                // Создаём инвестицию
                Investment investmentEntity = new Investment();
                investmentEntity.setStartupId(startup.getId());
                investmentEntity.setInvestorId(savedOffer.getInvestorId());
                investmentEntity.setAmount(savedOffer.getAmount());
                investmentEntity.setEquityPercent(equity);
                investmentEntity.setValuationPostMoney(valuationPostMoney);
                investmentEntity.setStatus("active");
                investmentEntity.setCreatedAt(Instant.now());
                investmentEntity.setUpdatedAt(Instant.now());

                investmentRepository.save(investmentEntity);
            }

            return ResponseEntity.ok(savedOffer);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }


}
