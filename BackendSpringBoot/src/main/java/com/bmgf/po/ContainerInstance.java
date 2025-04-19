package com.bmgf.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@AllArgsConstructor // 生成全参构造器
@NoArgsConstructor  // 生成无参构造器
@Data
@Document(collection = "container_instances")
public class ContainerInstance {

    @Id
    private String id;
    @JsonIgnore
    private String userId;
    private String labName; // 实验名称
    private String vulnType; // 漏洞类型
    private int restartCount; // 重启计数器
    private String lastStatusChangeReason; // 状态变更原因

    // 容器配置
    private String imageName;
    private Map<Integer, Integer> portMapping;
    private String containerId; // Docker容器ID

    // 网络ID，用于存储容器实例关联的网络ID
    private String networkId; // Docker 网络 ID

    // 状态管理
    private Instant createTime;
    private Instant expireTime; // 自动销毁时间
    private String status; // RUNNING/STOPPED/EXPIRED

    // 资源监控
    private Double cpuUsage; // CPU使用率
    private Long memoryUsage; // 内存使用量(字节)
}
