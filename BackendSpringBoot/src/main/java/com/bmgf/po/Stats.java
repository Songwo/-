package com.bmgf.po;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "stats")
public class Stats {
    @Id
    private String id;
    private String userId;        // 用户ID，如果是整站统计则为null
    private LocalDate date;       // 统计日期

    // 用户个人统计
    private int postCount;        // 发帖数
    private int commentCount;     // 评论数

    // 整站统计
    private long dailyVisits;     // 日访问量
    private long userCount;       // 用户总数
    private long totalPosts;      // 总帖子数
    private long totalComments;   // 总评论数

    @CreatedDate
    private LocalDate createdDate; // 创建时间

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getPostCount() {
        return postCount;
    }

    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public long getDailyVisits() {
        return dailyVisits;
    }

    public void setDailyVisits(long dailyVisits) {
        this.dailyVisits = dailyVisits;
    }

    public long getUserCount() {
        return userCount;
    }

    public void setUserCount(long userCount) {
        this.userCount = userCount;
    }

    public long getTotalPosts() {
        return totalPosts;
    }

    public void setTotalPosts(long totalPosts) {
        this.totalPosts = totalPosts;
    }

    public long getTotalComments() {
        return totalComments;
    }

    public void setTotalComments(long totalComments) {
        this.totalComments = totalComments;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }
}