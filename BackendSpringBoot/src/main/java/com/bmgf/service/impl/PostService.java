package com.bmgf.service.impl;
import com.bmgf.DTO.PostDTO;
import com.bmgf.po.Post;
import com.bmgf.dao.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;

    public List<Post> findAllPosts() {
        return postRepository.findAllByOrderByTimestampDesc();
    }
    public Post createPost(Post post) {
        return postRepository.save(post);
    }
    public void savePost(Post post) {
        postRepository.save(post);
    }
    public void deletePost(Post post) {
        postRepository.delete(post);
    }
    public Post findById(String id) {
        if(postRepository.findById(id).isEmpty()){
            return null;
        };
        return postRepository.findById(id).get();
    }
    @Autowired
    private MongoTemplate mongoTemplate;
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
