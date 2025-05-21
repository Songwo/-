package com.bmgf.service.impl;
import com.bmgf.po.ContainerInstance;
import com.bmgf.service.MySQLService;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.api.DockerClient;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
public class ContainerService {
    private final DockerClient dockerClient;
    private final MongoTemplate mongoTemplate;
    private final MySQLService mySQLService;

    public ContainerService(DockerClient dockerClient, MongoTemplate mongoTemplate, MySQLService mySQLService) {
        this.dockerClient = dockerClient;
        this.mongoTemplate = mongoTemplate;
        this.mySQLService = mySQLService;
    }

    private void createNetworkIfNotExists(String networkName) {
        boolean exists = dockerClient.listNetworksCmd()
                .exec()
                .stream()
                .anyMatch(n -> n.getName().equals(networkName));
        if (!exists) {
            dockerClient.createNetworkCmd().withName(networkName).exec();
        }
    }

    public ContainerInstance createSharedEnvironment(String userId, String vulnType, int durationMinutes) {
        try {
            String networkName = "shared-net-" + vulnType.replaceAll("[^a-zA-Z0-9]", "_");
            createNetworkIfNotExists(networkName);

            String dbName = "user_" + userId + "_" + vulnType.replaceAll("[^a-zA-Z0-9]", "_");
            mySQLService.createDatabaseIfNotExists(dbName);

            String imageName = vulnType;
            String containerName = vulnType.replaceAll("[^a-zA-Z0-9_.-]", "_");

            List<Container> containers = dockerClient.listContainersCmd()
                    .withNameFilter(List.of("/" + containerName))
                    .withShowAll(true)
                    .exec();

            String containerId;
            Integer hostPort = 3000;

            if (!containers.isEmpty()) {
                Container container = containers.get(0);
                if (!container.getStatus().toLowerCase().contains("up")) {
                    dockerClient.startContainerCmd(container.getId()).exec();
                }
                containerId = container.getId();
                for (ContainerPort port : container.getPorts()) {
                    if (port.getPrivatePort() == 3000 && port.getPublicPort() != null) {
                        hostPort = port.getPublicPort();
                        break;
                    }
                }
            } else {
                ExposedPort exposedPort = ExposedPort.tcp(3000);
                Ports portBindings = new Ports();
                portBindings.bind(exposedPort, Ports.Binding.empty());

                // 设置容器资源限制（内存、CPU、Swap 等）
                CreateContainerResponse container = dockerClient.createContainerCmd(imageName)
                        .withName(containerName)
                        .withExposedPorts(exposedPort)
                        .withHostConfig(HostConfig.newHostConfig()
                                .withPortBindings(portBindings)
                                .withNetworkMode(networkName)
                                .withMemory(512 * 1024 * 1024L)  // 设置内存为 512MB
                                .withMemorySwap(1L * 1024 * 1024 * 1024)  // 设置交换内存为 1GB
                                .withCpuShares(256)                // 设置 CPU 份额为 256
                                .withCpusetCpus("0")               // 限制容器只使用第 0 核
                        )
                        .withEnv(List.of(
                                "MYSQL_HOST=host.docker.internal",
                                "MYSQL_USER=jun",
                                "MYSQL_PASSWORD=123456",
                                "MYSQL_DB=" + dbName
                        ))
                        .exec();

                dockerClient.startContainerCmd(container.getId()).exec();
                containerId = container.getId();
                Container createdContainer = dockerClient.listContainersCmd()
                        .withIdFilter(List.of(containerId))
                        .withShowAll(true)
                        .exec()
                        .get(0);
                for (ContainerPort port : createdContainer.getPorts()) {
                    if (port.getPrivatePort() == 3000 && port.getPublicPort() != null) {
                        hostPort = port.getPublicPort();
                        break;
                    }
                }
            }

            ContainerInstance instance = new ContainerInstance();
            instance.setUserId(userId);
            instance.setLabName("共享环境-" + vulnType);
            instance.setVulnType(vulnType);
            instance.setRestartCount(0);
            instance.setLastStatusChangeReason("新建共享环境");
            instance.setImageName(imageName);
            instance.setContainerId(containerId);
            instance.setNetworkId(networkName);
            instance.setCreateTime(Instant.now());
            instance.setExpireTime(Instant.now().plus(durationMinutes, ChronoUnit.MINUTES));
            instance.setStatus("RUNNING");
            instance.setPortMapping(Map.of(3000, hostPort));
            instance.setAccessUrl("http://your-host-ip:" + hostPort);
            return mongoTemplate.save(instance);

        } catch (Exception e) {
            throw new RuntimeException("创建共享环境失败: " + e.getMessage(), e);
        }
    }
    // 定时任务，每两分钟执行一次
    @Scheduled(cron = "0 0/2 * * * ?") 
    public void removeExpiredSharedEnvironments() {
        try {
            List<ContainerInstance> instances = mongoTemplate.find(
                    query(where("expireTime").lt(Instant.now()).and("status").is("RUNNING")),
                    ContainerInstance.class);

            for (ContainerInstance instance : instances) {
                String containerId = instance.getContainerId();
                dockerClient.removeContainerCmd(containerId).withForce(true).exec();

                String dbName = instance.getUserId() + "_" + instance.getVulnType();
                mySQLService.dropDatabaseIfExists(dbName);

                mongoTemplate.remove(
                        query(where("containerId").is(containerId)),
                        ContainerInstance.class
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("定时销毁过期环境失败: " + e.getMessage(), e);
        }
    }
}
