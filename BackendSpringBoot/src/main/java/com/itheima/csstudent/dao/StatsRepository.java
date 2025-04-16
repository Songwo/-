package com.itheima.csstudent.dao;

import com.itheima.csstudent.po.Stats;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface StatsRepository extends MongoRepository<Stats, String> {
    Optional<Stats> findByDate(LocalDate date);
}