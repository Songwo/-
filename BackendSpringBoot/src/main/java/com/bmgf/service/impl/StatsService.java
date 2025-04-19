package com.bmgf.service.impl;

import com.bmgf.dao.StatsRepository;
import com.bmgf.po.Stats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class StatsService {

    @Autowired
    private StatsRepository statsRepository;

    public Map<String, Long> getStats() {
        LocalDate today = LocalDate.now();
        Stats stats = statsRepository.findByDate(today)
                .orElseGet(() -> {
                    Stats newStats = new Stats();
                    newStats.setDate(today);
                    newStats.setDailyVisits(0);
                    newStats.setUserCount(0);
                    return newStats;
                });

        Map<String, Long> result = new HashMap<>();
        result.put("dailyVisits", stats.getDailyVisits());
        result.put("userCount", stats.getUserCount());
        return result;
    }

    public void incrementVisits() {
        LocalDate today = LocalDate.now();
        Stats stats = statsRepository.findByDate(today)
                .orElseGet(() -> {
                    Stats newStats = new Stats();
                    newStats.setDate(today);
                    newStats.setDailyVisits(0);
                    newStats.setUserCount(0);
                    return newStats;
                });

        stats.setDailyVisits(stats.getDailyVisits() + 1);
        statsRepository.save(stats);
    }

    public void incrementUserCount() {
        LocalDate today = LocalDate.now();
        Stats stats = statsRepository.findByDate(today)
                .orElseGet(() -> {
                    Stats newStats = new Stats();
                    newStats.setDate(today);
                    newStats.setDailyVisits(0);
                    newStats.setUserCount(0);
                    return newStats;
                });

        stats.setUserCount(stats.getUserCount() + 1);
        statsRepository.save(stats);
    }
}
