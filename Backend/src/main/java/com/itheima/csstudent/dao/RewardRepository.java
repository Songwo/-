package com.itheima.csstudent.dao;

import com.itheima.csstudent.po.Reward;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;
public interface RewardRepository extends MongoRepository<Reward, String> {
}
