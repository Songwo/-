package com.bmgf.dao;

import com.bmgf.po.UserChallengeProgress;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserChallengeProgressRepository extends MongoRepository<UserChallengeProgress, String> {
}
