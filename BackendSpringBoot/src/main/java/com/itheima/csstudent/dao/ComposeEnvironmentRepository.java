package com.itheima.csstudent.dao;

import com.itheima.csstudent.po.ComposeEnvironment;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ComposeEnvironmentRepository extends MongoRepository<ComposeEnvironment, String> {
}
