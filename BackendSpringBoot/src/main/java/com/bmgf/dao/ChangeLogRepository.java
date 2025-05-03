package com.bmgf.dao;

import com.bmgf.po.Changelog;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ChangeLogRepository extends MongoRepository<Changelog, String> {
    List<Changelog> findByIsActiveTrueOrderByCreateTimeDesc();
    Changelog findByIdAndIsActiveTrue(String id);
}
