package com.bmgf.dao;

import com.bmgf.po.LearningContent;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface LearningContentRepository extends MongoRepository<LearningContent, String> {
    List<LearningContent> findByCategoriesInAndDifficultyLevelLessThanEqual(List<String> categories, Integer difficultyLevel);
    List<LearningContent> findByLearningStage(String learningStage);
    List<LearningContent> findByIdIn(List<String> ids);
}
