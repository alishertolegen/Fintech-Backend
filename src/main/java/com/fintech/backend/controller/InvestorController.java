package com.fintech.backend.controller;

import com.fintech.backend.model.Investor;
import com.fintech.backend.repository.InvestorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/investors")
public class InvestorController {

    private final InvestorRepository investorRepository;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public InvestorController(InvestorRepository investorRepository, MongoTemplate mongoTemplate) {
        this.investorRepository = investorRepository;
        this.mongoTemplate = mongoTemplate;
    }

    // Create
    @PostMapping
    public ResponseEntity<Investor> create(@RequestBody Investor investor) {
        investor.setCreatedAt(Instant.now());
        investor.setUpdatedAt(Instant.now());
        Investor saved = investorRepository.save(investor);
        return ResponseEntity.ok(saved);
    }

    // Read by id
    @GetMapping("/{id}")
    public ResponseEntity<Investor> getById(@PathVariable String id) {
        Optional<Investor> opt = investorRepository.findById(id);
        return opt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Read by userId
    @GetMapping("/user/{userId}")
    public ResponseEntity<Investor> getByUserId(@PathVariable String userId) {
        Optional<Investor> opt = investorRepository.findByUserId(userId);
        return opt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Update (replace)
    @PutMapping("/{id}")
    public ResponseEntity<Investor> update(@PathVariable String id, @RequestBody Investor payload) {
        return investorRepository.findById(id).map(existing -> {
            // copy fields (simple replace strategy)
            existing.setLegalName(payload.getLegalName());
            existing.setType(payload.getType());
            existing.setMinCheck(payload.getMinCheck());
            existing.setMaxCheck(payload.getMaxCheck());
            existing.setPreferredIndustries(payload.getPreferredIndustries());
            existing.setPreferredStages(payload.getPreferredStages());
            existing.setDescription(payload.getDescription());
            existing.setWebsite(payload.getWebsite());
            existing.setIsVerified(payload.getIsVerified());
            existing.setUpdatedAt(Instant.now());
            Investor saved = investorRepository.save(existing);
            return ResponseEntity.ok(saved);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (!investorRepository.existsById(id)) return ResponseEntity.notFound().build();
        investorRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Search / filter:
     * query params: type, industry, stage, minCheck (integer), maxCheck (integer)
     * Example: /api/investors?industry=fintech&minCheck=10000
     */
    @GetMapping
    public ResponseEntity<List<Investor>> search(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) Integer minCheck,
            @RequestParam(required = false) Integer maxCheck
    ) {
        Query q = new Query();

        if (type != null && !type.isBlank()) {
            q.addCriteria(Criteria.where("type").is(type));
        }
        if (industry != null && !industry.isBlank()) {
            q.addCriteria(Criteria.where("preferredIndustries").in(industry));
        }
        if (stage != null && !stage.isBlank()) {
            q.addCriteria(Criteria.where("preferredStages").in(stage));
        }
        if (minCheck != null) {
            // investor.maxCheck >= minCheck (investor can write checks up to maxCheck)
            q.addCriteria(Criteria.where("maxCheck").gte(minCheck));
        }
        if (maxCheck != null) {
            // investor.minCheck <= maxCheck
            q.addCriteria(Criteria.where("minCheck").lte(maxCheck));
        }

        q.with(Sort.by(Sort.Direction.DESC, "createdAt"));

        List<Investor> results = mongoTemplate.find(q, Investor.class);
        return ResponseEntity.ok(results);
    }
    @PutMapping("/user/{userId}")
    public ResponseEntity<Investor> updateByUserId(@PathVariable String userId, @RequestBody Investor payload) {
        Optional<Investor> opt = investorRepository.findByUserId(userId);
        if (opt.isPresent()) {
            Investor existing = opt.get();
            // копируем поля, как в вашем update(...)
            existing.setLegalName(payload.getLegalName());
            existing.setType(payload.getType());
            existing.setMinCheck(payload.getMinCheck());
            existing.setMaxCheck(payload.getMaxCheck());
            existing.setPreferredIndustries(payload.getPreferredIndustries());
            existing.setPreferredStages(payload.getPreferredStages());
            existing.setDescription(payload.getDescription());
            existing.setWebsite(payload.getWebsite());
            existing.setIsVerified(payload.getIsVerified());
            existing.setUpdatedAt(Instant.now());
            Investor saved = investorRepository.save(existing);
            return ResponseEntity.ok(saved);
        } else {
            // можно создать новый профиль инвестора привязанный к userId
            payload.setUserId(userId);
            payload.setCreatedAt(Instant.now());
            payload.setUpdatedAt(Instant.now());
            Investor saved = investorRepository.save(payload);
            return ResponseEntity.ok(saved);
        }
    }
}
