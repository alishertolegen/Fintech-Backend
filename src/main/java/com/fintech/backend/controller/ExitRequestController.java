package com.fintech.backend.controller;

import com.fintech.backend.model.ExitRequest;
import com.fintech.backend.model.Investment;
import com.fintech.backend.repository.ExitRequestRepository;
import com.fintech.backend.repository.InvestmentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/exit-requests")
public class ExitRequestController {

    @Autowired
    private ExitRequestRepository repository;

    @Autowired
    private InvestmentRepository investmentRepository;

    // 🔹 Получить exit requests
    @GetMapping
    public List<ExitRequest> getByStartup(@RequestParam String startupId) {
        return repository.findByStartupId(startupId);
    }

    // 🔹 Получить по investmentId
    @GetMapping("/investment/{investmentId}")
    public List<ExitRequest> getByInvestment(@PathVariable String investmentId) {
        return repository.findByInvestmentId(investmentId);
    }

    // 🔹 Создать запрос выхода (инвестор)
    @PostMapping
    public ExitRequest create(@RequestBody ExitRequest request) {

        // проверка: инвестиция существует?
        Investment inv = investmentRepository
                .findById(request.getInvestmentId())
                .orElseThrow(() -> new RuntimeException("Investment not found"));

        // проверка: уже завершена?
        if ("completed".equalsIgnoreCase(inv.getStatus())) {
            throw new RuntimeException("Investment already completed");
        }

        request.setStatus("PENDING");
        request.setPaymentStatus("PENDING");
        request.setCreatedAt(Instant.now());
        request.setStartupId(inv.getStartupId());
        return repository.save(request);
    }

    // 🔥 Принять (стартапер)
    @PatchMapping("/{id}/accept")
    public ExitRequest accept(@PathVariable String id) {

        ExitRequest req = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exit request not found"));

        if (!"PENDING".equalsIgnoreCase(req.getStatus())) {
            throw new RuntimeException("Request already processed");
        }

        Investment inv = investmentRepository.findById(req.getInvestmentId())
                .orElseThrow(() -> new RuntimeException("Investment not found"));

        // 🔥 имитация оплаты
        req.setStatus("ACCEPTED");
        req.setPaymentStatus("PAID");

        // 🔥 закрываем инвестицию
        inv.setStatus("completed");
        inv.setUpdatedAt(Instant.now());

        investmentRepository.save(inv);

        return repository.save(req);
    }

    // ❌ Отклонить (стартапер)
    @PatchMapping("/{id}/reject")
    public ExitRequest reject(@PathVariable String id) {

        ExitRequest req = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exit request not found"));

        if (!"PENDING".equalsIgnoreCase(req.getStatus())) {
            throw new RuntimeException("Request already processed");
        }

        req.setStatus("REJECTED");

        return repository.save(req);
    }

    // 🔹 Удалить (опционально)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        repository.deleteById(id);
    }
}