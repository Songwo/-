package com.itheima.csstudent.dao;
import com.itheima.csstudent.po.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface CommentRepository extends MongoRepository<Comment, String> {
    List<Comment> findByPostIdOrderByTimestampDesc(String postId);
    Comment findFirstByPostIdOrderByTimestampDesc(String postId);
}