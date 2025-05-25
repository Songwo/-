// User.java 用户类增强
package com.bmgf.po;

import jakarta.validation.constraints.Email;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.*;

@Data
@Document(collection = "user")
public class User implements Serializable {
    // 基础信息
    private String id;
    @CreatedDate
    private Date createTime;
    @LastModifiedDate
    private Date updateTime;

    // 认证信息
    @NotBlank
    @Indexed(unique = true)
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9_]{3,30}$",
            message = "用户名需为3-30位中文、字母、数字或下划线")
    private String username;
    private String password;
    private String avatar="avatar/17fd8e0a-2bb9-4f82-934d-3f8820fca5f5_ab.jpg";
    // 联系信息
    @Indexed
    @Email(regexp = "^(?=.{1,64}@)[a-zA-Z0-9_+&*-]+(\\.[a-zA-Z0-9_+&*-]+)*@"
            + "[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*(\\.[a-zA-Z]{2,})$",
            message = "邮箱格式不合法")
    private String email;
    private Boolean emailVerified = false;
    private String emailVerificationToken;
    private Date emailVerificationTokenExpiry;
    // 安全相关
    private Set<String> roles = new HashSet<>(Arrays.asList("USER")); // 用户角色
    private Date lastLoginTime; // 最后登录时间
    // 积分系统
    @Indexed(name = "score_index", direction = IndexDirection.DESCENDING)
    private int totalScore = 0; // 总积分
    private int correctCount = 0; // 正确次数
    private int totalAttempts = 0; // 总尝试次数
    private List<String> completedQuestions = new ArrayList<>();
    private Map<String, Integer> vulnScores = new HashMap<>(); // 各漏洞类型得分
    private Map<String, Integer> HonoraryTitle = new HashMap<>();
    private int  ActivityPoints;//活动积分
    private Date lastCheckInDate; // 上次签到日期
    private int consecutiveCheckInDays = 0; // 连续签到天数
    private Set<String> ownedRooms = new HashSet<>();//聊天室
    // 实验管理
    @DBRef
    private List<ContainerInstance> activeLabs = new ArrayList<>(); // 正在进行的实验
    private List<String> completedLabs = new ArrayList<>(); // 已完成实验ID

}