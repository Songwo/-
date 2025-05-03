package com.bmgf.service.impl;
import com.bmgf.DTO.PostDTO;
import com.bmgf.po.Post;
import com.bmgf.dao.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    // 获取所有帖子（按时间倒序）
    public List<Post> findAllPosts() {
        return postRepository.findAllByOrderByTimestampDesc();
    }

    // 创建帖子
    public Post createPost(Post post) {
        return postRepository.save(post);
    }

    // 更新帖子
    public void savePost(Post post) {
        postRepository.save(post);
    }

    // 删除帖子
    public void deletePost(Post post) {
        postRepository.delete(post);
    }

    // 根据ID查找帖子
    public Post findById(String id) {
        Optional<Post> post = postRepository.findById(id);
        return post.orElse(null);
    }

    // 按板块查询帖子
    public List<Post> findBySection(String section) {
        Query query = new Query(Criteria.where("section").is(section))
                .with(Sort.by(Sort.Direction.DESC, "timestamp"));
        return mongoTemplate.find(query, Post.class);
    }

    // 搜索帖子（标题、内容、用户名）
    public List<Post> searchPosts(String keyword) {
        Query query = new Query();
        Criteria criteria = new Criteria().orOperator(
                Criteria.where("title").regex(keyword, "i"),
                Criteria.where("content").regex(keyword, "i"),
                Criteria.where("username").regex(keyword, "i")
        );
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.DESC, "timestamp"));
        return mongoTemplate.find(query, Post.class);
    }

    // 获取热门帖子（按回复数排序）
    public List<Post> findHotPosts(int limit) {
        Query query = new Query();
        query.with(Sort.by(Sort.Direction.DESC, "replyCount"));
        query.limit(limit);
        return mongoTemplate.find(query, Post.class);
    }

    // 统计各板块帖子数量
    public List<PostDTO> countPostBySection() {
        TypedAggregation<Post> aggregation = Aggregation.newAggregation(
                Post.class,
                Aggregation.group("section").count().as("count"),
                Aggregation.project("count").and("_id").as("section")
        );
        AggregationResults<PostDTO> results =
                mongoTemplate.aggregate(aggregation, PostDTO.class);
        return results.getMappedResults();
    }
}