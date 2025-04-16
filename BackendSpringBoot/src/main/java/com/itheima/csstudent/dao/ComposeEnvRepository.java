package com.itheima.csstudent.dao;

import com.itheima.csstudent.po.ComposeEnvironment;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ComposeEnvRepository extends MongoRepository<ComposeEnvironment, String> {
    ComposeEnvironment findByUserId(String userId);
}

