package com.bmgf.dao;

import com.bmgf.po.Announcement;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface AnnouncementRepository extends MongoRepository<Announcement, String> {
    List<Announcement> findByIsActiveTrueOrderByCreateTimeDesc();
    Announcement findByIdAndIsActiveTrue(String id);
}
