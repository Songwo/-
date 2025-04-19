package com.bmgf.dao;
import com.bmgf.po.Question;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import java.util.List;
import java.util.Set;

public interface QuestionRepository extends MongoRepository<Question, String> {
    // 使用 @Aggregation 定义聚合管道
//    @Aggregation(pipeline = {
//            "{ $sample: { size: 10 } }",          // 随机取10条
//            "{ $project: { answer: 0 } }"         // 排除答案字段
//    })
//    List<Question> findRandomQuestions();
    @Aggregation(pipeline = {
            "{ $match: { _id: { $nin: ?0 }}}",// 排除已答题目
            "{ $sample: { size: ?1 } }"        // 随机抽取指定数量的题目
    })
    List<Question> findRandomQuestionsExcluding(Set<String> excludedIds, int size);

    @Query("{'category': ?0}")
//?0表示参数的占位符
    List<Question> findByCategory(String category);

    @Aggregation(pipeline = {
            "{ $match: { difficulty: ?0, _id: { $nin: ?1 } } }", // 按难度筛选并排除已答题目
            "{ $sample: { size: ?2 } }"                          // 随机抽取指定数量的题目
    })
    List<Question> findRandomQuestionsByDifficultyExcluding(String difficulty, Set<String> excludedIds, int size);

    @Query("{ _id: ?0 }")
    @Update("{ $inc: { difficultyLevel: 1 } }")
        // 增加难度
    void incrementDifficulty(String questionId);

    @Query("{ _id: ?0 }")
    @Update("{ $inc: { difficultyLevel: -1 } }")
        // 降低难度
    void decrementDifficulty(String questionId);
}