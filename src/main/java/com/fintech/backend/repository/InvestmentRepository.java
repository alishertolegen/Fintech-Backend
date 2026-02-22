package com.fintech.backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.fintech.backend.model.Investment;
import java.util.List;

public interface InvestmentRepository extends MongoRepository<Investment, String> {
    List<Investment> findByInvestorId(String investorId);
    List<Investment> findByStartupId(String startupId);
}
