package com.itheima.csstudent.po;
// ContainerInstance.java 容器实例类
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.Map;
@AllArgsConstructor // 生成全参构造器（不包含 accuracy）
@NoArgsConstructor  // 生成无参构造器
@Data
@Document(collection = "container_instances")
public class ContainerInstance {
    @Id
    private String id;
    @JsonIgnore
    @DBRef
    private User user; // 关联用户

    private String labName; // 实验名称
    private String vulnType; // 漏洞类型
    private int restartCount; // 重启计数器
    private String lastStatusChangeReason; // 状态变更原因
    // 容器配置
    private String imageName;
    private Map<Integer, Integer> portMapping;
    private String containerId; // Docker容器ID

    // 状态管理
    private Instant createTime;
    private Instant expireTime; // 自动销毁时间
    private String status; // RUNNING/STOPPED/EXPIRED

    // 资源监控
    private Double cpuUsage; // CPU使用率
    private Long memoryUsage; // 内存使用量(字节)
    // 显式定义 getter 方法
    public String getContainerId() {
        return this.containerId;
    }

    public String getId() {
        return this.id;
    }

    public User getUser() {
        return this.user;
    }
}