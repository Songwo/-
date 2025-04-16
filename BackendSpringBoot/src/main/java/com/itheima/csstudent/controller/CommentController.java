package com.itheima.csstudent.controller;
import com.itheima.csstudent.po.Comment;
import com.itheima.csstudent.po.Result;
import com.itheima.csstudent.service.impl.CommentService;
import com.itheima.csstudent.service.impl.UserService;
import com.itheima.csstudent.service.impl.VulnContainerService;
import com.itheima.csstudent.util.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
//@CrossOrigin(origins = "http://54.193.5.242", methods = {RequestMethod.GET, RequestMethod.POST,RequestMethod.PUT}, allowCredentials = "true")
@RequestMapping("/comments")
public class CommentController {
    @Autowired
    private CommentService commentService;
    // 2. 根据postId查询最新评论
    @GetMapping("/find/{postId}")
    public Result findLatestComment(@Valid @PathVariable String postId) {
        return Result.success(commentService.getCommentsByPostId(postId));
    }
    @Autowired
    private VulnContainerService containerService;
    @Autowired
    private UserService userService;
    // 3. 插入评论
    @PostMapping("/insert")
    public Result insertComment(@Valid @RequestBody Comment comment, @RequestHeader("Authorization") String authHeader) {
        // 从安全上下文获取当前用户ID
        String token = authHeader.substring(7); // 去掉"Bearer "前缀
        String username = containerService.getUsernameFromToken(token);
        // 其他逻辑不变...
        comment.setAuthorId(userService.findByUsername(username).getId());
        return Result.success(commentService.addComment(comment));
    }
}

