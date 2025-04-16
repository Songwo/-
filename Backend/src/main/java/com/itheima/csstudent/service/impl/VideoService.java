package com.itheima.csstudent.service.impl;
import com.itheima.csstudent.DTO.CategoryCount;
import com.itheima.csstudent.DTO.CategoryVideoDTO;
import com.itheima.csstudent.dao.VideoRepository;
import com.itheima.csstudent.po.Question;
import com.itheima.csstudent.po.VideoItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VideoService {
    @Autowired
    private VideoRepository videoRepository;
    public Page<VideoItem> getVideos(Pageable pageable, String keyword) {
        // 直接调用 Repository 方法
        return videoRepository.findAllByOrderByCreateTimeDesc(pageable,keyword);
    }
    public Page<VideoItem> getVideo(Pageable pageable) {
        // 直接调用 Repository 方法
        return videoRepository.findAllByOrderByCreateTimeDesc(pageable);
    }
    @Autowired
    private MongoTemplate mongoTemplate;
    public List<CategoryVideoDTO> countVideoByCategory() {
        TypedAggregation<VideoItem> aggregation = Aggregation.newAggregation(
                VideoItem.class,
                Aggregation.group("categories").count().as("count"),
                Aggregation.project("count").and("_id").as("categories")
        );
        AggregationResults<CategoryVideoDTO> results =
                mongoTemplate.aggregate(aggregation, CategoryVideoDTO.class);
        return results.getMappedResults();
    }
    public void deleteVideo(String id){
        videoRepository.deleteById(id);
    }
    public void saveVideo(VideoItem videoItem) {
        mongoTemplate.save(videoItem);
    }
    public VideoItem findById(String id) {
        if(videoRepository.findById(id).isEmpty())
            return null;
        return videoRepository.findById(id).get();
    }
}


