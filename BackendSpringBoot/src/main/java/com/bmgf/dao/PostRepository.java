package com.bmgf.dao;
import com.bmgf.po.Post;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
public interface  PostRepository extends MongoRepository<Post, String> {
    List<Post> findAllByOrderByTimestampDesc();

}