package com.itheima.csstudent.dao;

import com.itheima.csstudent.po.LabFlag;
import com.itheima.csstudent.po.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface LabFlagRepository extends MongoRepository<LabFlag,String> {
    @Query("{'username': ?0}")//?0表示参数的占位符
    Optional<LabFlag> findByImageName(String ImageName);
}
