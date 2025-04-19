package com.bmgf.service.impl;
import com.bmgf.po.Comment;
import com.bmgf.dao.CommentRepository;
import com.bmgf.po.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;
    public Comment getLatestCommentByPostId(String postId) {
        return commentRepository.findFirstByPostIdOrderByTimestampDesc(postId);
    }
    public List<Comment> getCommentsByPostId(String postId) {
        return commentRepository.findByPostIdOrderByTimestampDesc(postId);
    }
    @Autowired
    private MongoTemplate mongoTemplate;
    public Comment addComment(Comment comment) {
        // 保存评论
        Comment savedComment = mongoTemplate.save(comment);
        // 原子递增对应帖子的回复数
        Update update = new Update().inc("replyCount", 1);
        mongoTemplate.updateFirst(
                Query.query(Criteria.where("id").is(comment.getPostId())),
                update,
                Post.class
        );
        return savedComment;
    }
    public void save(Comment comment) {
        commentRepository.save(comment);
    }
    public void delete(Comment comment) {
        commentRepository.delete(comment);
    }
    public Comment findById(String id) {
        if(commentRepository.findById(id).isEmpty()){
            return null;
        };
        return commentRepository.findById(id).get();
    }
    public void deleteCommentById(String id){
        commentRepository.deleteById(id);
    }
}
