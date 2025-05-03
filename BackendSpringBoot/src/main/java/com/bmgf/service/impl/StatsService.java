package com.bmgf.service.impl;

import com.bmgf.dao.StatsRepository;
import com.bmgf.po.Stats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class StatsService {

    @Autowired
    private StatsRepository statsRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public Map<String, Long> getStats() {
        LocalDate today = LocalDate.now();
        Stats stats = statsRepository.findByDate(today)
                .orElseGet(() -> {
                    Stats newStats = new Stats();
                    newStats.setDate(today);
                    newStats.setDailyVisits(0);
                    newStats.setUserCount(0);
                    return statsRepository.save(newStats);
                });

        Map<String, Long> result = new HashMap<>();
        result.put("dailyVisits", stats.getDailyVisits());
        result.put("userCount", stats.getUserCount());
        return result;
    }

    public void incrementVisits() {
        LocalDate today = LocalDate.now();
        Query query = new Query(Criteria.where("date").is(today));
        Update update = new Update()
                .inc("dailyVisits", 1)
                .setOnInsert("date", today)
                .setOnInsert("userCount", 0);

        mongoTemplate.upsert(query, update, Stats.class);
    }

    public void incrementUserCount() {
        LocalDate today = LocalDate.now();
        Query query = new Query(Criteria.where("date").is(today));
        Update update = new Update()
                .inc("userCount", 1)
                .setOnInsert("date", today)
                .setOnInsert("dailyVisits", 0);

        mongoTemplate.upsert(query, update, Stats.class);
    }

    // 获取用户统计信息
    public Map<String, Integer> getUserStats(String username) {
        // 使用 MongoTemplate 查询用户的帖子数和评论数
        Query postQuery = new Query(Criteria.where("username").is(username));
        Query commentQuery = new Query(Criteria.where("username").is(username));

        long postCount = mongoTemplate.count(postQuery, "posts");
        long commentCount = mongoTemplate.count(commentQuery, "comments");

        Map<String, Integer> stats = new HashMap<>();
        stats.put("postCount", (int) postCount);
        stats.put("commentCount", (int) commentCount);

        return stats;
    }

    // 更新用户发帖统计
    public void incrementUserPostCount(String userId) {
        LocalDate today = LocalDate.now();
        Query query = new Query(Criteria.where("userId").is(userId).and("date").is(today));
        Update update = new Update()
                .inc("postCount", 1)
                .setOnInsert("date", today)
                .setOnInsert("userId", userId)
                .setOnInsert("commentCount", 0);

        mongoTemplate.upsert(query, update, Stats.class);
    }

    // 更新用户评论统计
    public void incrementUserCommentCount(String userId) {
        LocalDate today = LocalDate.now();
        Query query = new Query(Criteria.where("userId").is(userId).and("date").is(today));
        Update update = new Update()
                .inc("commentCount", 1)
                .setOnInsert("date", today)
                .setOnInsert("userId", userId)
                .setOnInsert("postCount", 0);

        mongoTemplate.upsert(query, update, Stats.class);
    }
}