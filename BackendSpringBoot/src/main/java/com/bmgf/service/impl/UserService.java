package com.bmgf.service.impl;
import com.bmgf.DTO.UserScoreDto;
import com.bmgf.po.User;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import com.bmgf.dao.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.query.Query;
import java.util.List;
@Service
public class UserService {
    @Autowired
    private UserRepository userService;
    //通过id查找全部信息
    public User findByIdINt(String i){
        if(userService.findByIdString(i).isEmpty()){
            return null;
        }
        else {
            return userService.findByIdString(i).get();
        }
    }
    //保存
    public void save(User user) {
        userService.save(user);
    }
    //通过用户姓名查找
    public User findByUsername(String username) {
        if(userService.findByUsername(username).isEmpty()){
            return null;
        }
        else {
            return userService.findByUsername(username).get();
        }
    }
    @Autowired
    private MongoTemplate mongoTemplate;
    public List<User> getTop100UsersByScore() {
        // 构建查询条件
        Query query = new Query()
                .with(Sort.by(Sort.Direction.DESC, "totalScore")) // 按 totalScore 降序
                .limit(100); // 限制返回100条
        // 执行查询
        return mongoTemplate.find(query, User.class);
    }
    public void delete(User user) {
        userService.delete(user);
    }

    public List<UserScoreDto> findAllTotalScore() {
        // 1. 指定包含的字段
        ProjectionOperation project = Aggregation.project("totalScore", "username");
        // 2. 构建聚合管道
        Aggregation aggregation = Aggregation.newAggregation(project);
        // 3. 执行查询
        return mongoTemplate.aggregate(aggregation, "user", UserScoreDto.class)
                .getMappedResults();
    }
}
