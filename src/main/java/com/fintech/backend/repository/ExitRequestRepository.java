package com.fintech.backend.repository;

import com.fintech.backend.model.ExitRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ExitRequestRepository extends MongoRepository<ExitRequest, String> {

    // получить все запросы по инвестиции
    List<ExitRequest> findByInvestmentId(String investmentId);
    List<ExitRequest> findByStatus(String status);
    List<ExitRequest> findByInvestmentIdAndStatus(String investmentId, String status);

    List<ExitRequest> findByStartupId(String startupId);
}