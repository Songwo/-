package com.bmgf.service.impl;
import com.bmgf.DTO.UserScoreDto;
import com.bmgf.po.Comment;
import com.bmgf.po.HonoraryTitle;
import com.bmgf.po.Post;
import com.bmgf.po.User;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import com.bmgf.dao.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.StringUtils;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.List;
import java.util.Date;
import java.util.UUID;


@Service
public class UserService {
    @Autowired
    private HonoraryTitleService honoraryTitleService;
    @Autowired
    private UserRepository userService;
    @Autowired
    private EmailService emailService;

    public void sendVerificationEmail(String userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 生成验证token
        String token = UUID.randomUUID().toString();
        Date expiryDate = new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000); // 24小时后过期

        // 更新用户验证信息
        user.setEmailVerificationToken(token);
        user.setEmailVerificationTokenExpiry(expiryDate);
        userService.save(user);

        // 发送验证邮件
        System.out.println(user.getEmail());
        emailService.sendVerificationEmail(user.getEmail(), token);
    }

    public boolean verifyEmail(String token) {
        User user = userService.findByEmailVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));

        // 检查token是否过期
        if (user.getEmailVerificationTokenExpiry().before(new Date())) {
            throw new RuntimeException("Verification token has expired");
        }

        // 更新用户验证状态
        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiry(null);
        userService.save(user);

        return true;
    }
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
    public Map<String, Integer> findByIdHonoraryTitle(String id) {
        if (!StringUtils.hasText(id)){
            return Collections.emptyMap();
        }
        Query query = new Query(Criteria.where("id").is(id));
        query.fields().include("HonoraryTitle");
        User user = mongoTemplate.findOne(query, User.class);
        return user != null ? user.getHonoraryTitle() : Collections.emptyMap();
    }
    public boolean selectUserHonoraryTitle(String honoraryTitle, String username) {
        if (!StringUtils.hasText(honoraryTitle)) {
            return false;
        }

        HonoraryTitle foundTitle = mongoTemplate.findOne(
                Query.query(Criteria.where("title").is(honoraryTitle)),
                HonoraryTitle.class
        );
        if (foundTitle == null) {
            return false;
        }

        Optional<User> optionalUser = userService.findByUsername(username);
        if (!optionalUser.isPresent()) {
            return false;
        }
        User user = optionalUser.get();
        Map<String, Integer> titleMap = user.getHonoraryTitle();
        titleMap.replaceAll((k, v) -> k.equals(honoraryTitle) ? 1 : 0);
        user.setHonoraryTitle(titleMap);
        userService.save(user);
        Query userQuery = Query.query(Criteria.where("username").is(username));
        Update update = new Update().set("honoraryTitle", honoraryTitle);
        mongoTemplate.updateMulti(userQuery, update, Post.class);
        mongoTemplate.updateMulti(userQuery, update, Comment.class);

        return true;
    }

    public boolean insertUserHonoraryTitle(String honoraryTitle, String username, String points) {
        if (!StringUtils.hasText(honoraryTitle)) {
            return false;
        }
        HonoraryTitle titleDoc = mongoTemplate.findOne(
                Query.query(Criteria.where("title").is(honoraryTitle)),
                HonoraryTitle.class
        );
        if (titleDoc == null) {
            return false;
        }
        Optional<User> optionalUser = userService.findByUsername(username);
        if (!optionalUser.isPresent()) {
            return false;
        }
        User user = optionalUser.get();
        int costPoints;
        try {
            costPoints = Integer.parseInt(points);
        } catch (NumberFormatException e) {
            System.out.println("points 不合法: " + points);
            return false;
        }
        int currentPoints = user.getActivityPoints();
        if (currentPoints < costPoints) {
            System.out.println("用户积分不足");
            return false;
        }
        Map<String, Integer> map = user.getHonoraryTitle();
        if (!map.containsKey(honoraryTitle)) {
            map.put(honoraryTitle, 0);
        }
        user.setActivityPoints(currentPoints - costPoints);
        user.setHonoraryTitle(map);
        userService.save(user);
        return true;
    }
    public boolean checkIn(String username) {
        Optional<User> optionalUser = userService.findByUsername(username);
        if (!optionalUser.isPresent()) return false;
        User user = optionalUser.get();
        LocalDate today = LocalDate.now();
        LocalDate lastDate = null;
        if (user.getLastCheckInDate() != null) {
            lastDate = user.getLastCheckInDate().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            if (lastDate.equals(today)) {
                // 已经签到过了
                return false;
            }
        }
        // 签到成功：更新时间 & 活跃积分 +10
        user.setLastCheckInDate(Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        user.setActivityPoints(user.getActivityPoints() + 10);
        // 连续签到逻辑
        if (lastDate != null && lastDate.plusDays(1).equals(today)) {
            user.setConsecutiveCheckInDays(user.getConsecutiveCheckInDays() + 1);
        } else {
            user.setConsecutiveCheckInDays(1);
        }
        userService.save(user);
        return true;
    }
    public boolean addPoints(String username, int points) {
        Optional<User> u = userService.findByUsername(username);
        if (u.isEmpty()) {
            return false;
        }
        User user = u.get();
        user.setActivityPoints(user.getActivityPoints() + points);
        userService.save(user);
        return true;
    }
}
