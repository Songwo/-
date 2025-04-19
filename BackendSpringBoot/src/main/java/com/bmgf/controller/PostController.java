package com.bmgf.controller;
import com.bmgf.po.Result;
import com.bmgf.po.Post;
import com.bmgf.service.impl.PostService;
import com.bmgf.service.impl.UserService;
import com.bmgf.service.impl.VulnContainerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/post")
public class PostController {
    @Autowired
    private PostService postService;
    // 1. 查询全部帖子
    @GetMapping("/findAll")
    public Result findAllPosts() {
        List<Post> posts = postService.findAllPosts();
        return Result.success(posts);
    }
    @Autowired
    private VulnContainerService containerService;
    @Autowired
    private UserService userService;
    @GetMapping("/findById")
    public Result findPostById(@RequestParam String id) {
        return Result.success(postService.findById(id));
    }
    // 4. 插入帖子
    @PostMapping("/insertPost")
    public Result insertPost(@Valid @RequestBody Post post, @RequestHeader("Authorization") String authHeader) {
        // 强制使用当前用户ID
        String token = authHeader.substring(7); // 去掉"Bearer "前缀
        String username = containerService.getUsernameFromToken(token);
        // 其他逻辑不变...
        post.setAuthorId(userService.findByUsername(username).getId());
        return Result.success(postService.createPost(post));
    }
}
