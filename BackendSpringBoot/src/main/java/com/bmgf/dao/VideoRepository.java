package com.bmgf.dao;
import com.bmgf.po.VideoItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface VideoRepository extends MongoRepository<VideoItem, String> {
    // MongoDB 分页查询（按创建时间倒序）
    @Query("{'categories': ?0}")
    Page<VideoItem> findAllByOrderByCreateTimeDesc(Pageable pageable, String categories);
    Page<VideoItem> findAllByOrderByCreateTimeDesc(Pageable pageable);
}
