package com.itheima.csstudent.service.impl;

import com.itheima.csstudent.DTO.CategoryCount;
import com.itheima.csstudent.dao.QuestionRepository;
import com.itheima.csstudent.po.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class QuestionService {



    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private QuestionRepository questionRepository;

    public List<CategoryCount> countQuestionsByCategory() {
        TypedAggregation<Question> aggregation = Aggregation.newAggregation(
                Question.class,
                Aggregation.group("category").count().as("count"),
                Aggregation.project("count").and("_id").as("category")
        );
        AggregationResults<CategoryCount> results =
                mongoTemplate.aggregate(aggregation, CategoryCount.class);
        return results.getMappedResults();
    }
    public void saveQuestion(Question question) {
        mongoTemplate.save(question);
    }
    public List<Question> findAll() {
        return mongoTemplate.findAll(Question.class);
    }
    public Question findById(String id) {
        if(questionRepository.findById(id).isEmpty())
            return null;
        return questionRepository.findById(id).get();
    }
    public void deleteQuestion(String id) {
        questionRepository.deleteById(id);
    }
}
