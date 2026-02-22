package com.fintech.backend.repository;

import com.fintech.backend.model.Investor;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface InvestorRepository extends MongoRepository<Investor, String> {
    Optional<Investor> findByUserId(String userId);
    List<Investor> findByType(String type);
    // Быстрые методы, но для сложных фильтров используем MongoTemplate в контроллере
}
