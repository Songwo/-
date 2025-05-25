package com.bmgf.dao;

import com.bmgf.po.Feedback;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface FeedbackRepository extends MongoRepository<Feedback, String> {
    Page<Feedback> findByStatus(String status, Pageable pageable);
    long countByStatus(String status);
}
