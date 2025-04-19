package com.bmgf.dao;

import com.bmgf.po.Challenge;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ChallengeRepository extends MongoRepository<Challenge, String> {
    // 可以自定义查询方法
    // ChallengeRepository.java
    @Query("{'images.$*.image': ?0}")
    List<Challenge> findByAnyImageName(String imageName);
}