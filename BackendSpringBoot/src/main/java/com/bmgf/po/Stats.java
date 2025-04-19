package com.bmgf.po;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "stats")
public class Stats {
    @Id
    private String id;
    private LocalDate date;
    private long dailyVisits;
    private long userCount;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
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
}
