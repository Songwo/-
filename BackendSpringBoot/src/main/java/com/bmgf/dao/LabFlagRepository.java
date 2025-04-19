package com.bmgf.dao;

import com.bmgf.po.LabFlag;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface LabFlagRepository extends MongoRepository<LabFlag,String> {
    @Query("{'imageName': ?0}")//?0表示参数的占位符
    Optional<LabFlag> findByImageName(String ImageName);
}
