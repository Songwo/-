package com.itheima.csstudent.dao;
import com.itheima.csstudent.po.Post;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
public interface  PostRepository extends MongoRepository<Post, String> {
    List<Post> findAllByOrderByTimestampDesc();

}