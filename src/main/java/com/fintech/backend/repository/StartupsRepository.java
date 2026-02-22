package com.fintech.backend.repository;

import com.fintech.backend.model.Startup;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StartupsRepository extends MongoRepository<Startup, String> {
    Optional<Startup> findBySlug(String slug);
    boolean existsBySlug(String slug);
    List<Startup> findAllByFounderId(String founderId);
    List<Startup> findAllByStage(String stage);
    List<Startup> findAllByStageAndIndustry(String stage, String industry);
}
