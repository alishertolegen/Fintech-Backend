package com.fintech.backend.repository;

import com.fintech.backend.model.Offer;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OfferRepository extends MongoRepository<Offer, String> {
    List<Offer> findByStartupId(String startupId);
    List<Offer> findByInvestorId(String investorId);
    List<Offer> findByStatus(String status);
    List<Offer> findByVisibility(String visibility);
    // при необходимости — добавить сложные запросы / агрегации
}
