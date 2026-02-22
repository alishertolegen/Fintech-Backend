package com.fintech.backend.repository;

import com.fintech.backend.model.StartupMetric;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface StartupMetricsRepository extends MongoRepository<StartupMetric, String> {
    List<StartupMetric> findAllByStartupIdOrderByDateDesc(String startupId);
    List<StartupMetric> findAllByStartupIdAndDateBetweenOrderByDateDesc(String startupId, Instant from, Instant to);
    List<StartupMetric> findAllByStartupId(String startupId);
}
