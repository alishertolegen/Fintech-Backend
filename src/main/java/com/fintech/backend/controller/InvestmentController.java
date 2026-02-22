package com.fintech.backend.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.fintech.backend.model.Investment;
import com.fintech.backend.repository.InvestmentRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/investments")
public class InvestmentController {

    @Autowired
    private InvestmentRepository repository;

    @GetMapping
    public List<Investment> getAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Optional<Investment> getById(@PathVariable String id) {
        return repository.findById(id);
    }

    @GetMapping("/investor/{investorId}")
    public List<Investment> getByInvestor(@PathVariable String investorId) {
        return repository.findByInvestorId(investorId);
    }

    @GetMapping("/startup/{startupId}")
    public List<Investment> getByStartup(@PathVariable String startupId) {
        return repository.findByStartupId(startupId);
    }

    @PostMapping
    public Investment create(@RequestBody Investment investment) {
        investment.setCreatedAt(Instant.now());
        investment.setUpdatedAt(Instant.now());
        return repository.save(investment);
    }

    @PutMapping("/{id}")
    public Investment update(@PathVariable String id, @RequestBody Investment updated) {
        return repository.findById(id).map(inv -> {
            inv.setAmount(updated.getAmount());
            inv.setCurrency(updated.getCurrency());
            inv.setEquityPercent(updated.getEquityPercent());
            inv.setValuationPostMoney(updated.getValuationPostMoney());
            inv.setStatus(updated.getStatus());
            inv.setNote(updated.getNote());
            inv.setUpdatedAt(Instant.now());
            return repository.save(inv);
        }).orElse(null);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        repository.deleteById(id);
    }
}
