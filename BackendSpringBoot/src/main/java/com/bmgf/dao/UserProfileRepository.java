package com.bmgf.dao;

import com.bmgf.po.UserProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface UserProfileRepository extends MongoRepository<UserProfile, String> {
    UserProfile findByUserId(String userId);
    List<UserProfile> findByInterestsIn(List<String> interests);
}
