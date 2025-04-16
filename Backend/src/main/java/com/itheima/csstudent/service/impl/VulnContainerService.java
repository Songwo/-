package com.itheima.csstudent.service.impl;

import com.github.dockerjava.api.command.CreateNetworkResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.exception.NotModifiedException;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.itheima.csstudent.po.ComposeEnvironment;
import com.itheima.csstudent.po.ContainerInstance;
import com.itheima.csstudent.po.User;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.itheima.csstudent.util.JwtUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VulnContainerService {
    private final DockerClient dockerClient;
    private final MongoTemplate mongoTemplate;
    private final JwtUtil jwtUtil;

    private static final int MAX_RESTART_ATTEMPTS = 3;
    private static final int MONITOR_INTERVAL = 2 * 60 * 1000;
    private static final int MYSQL_WAIT_TIMEOUT = 120;
    private static final int SERVICE_START_DELAY = 20;
    private static final Logger log = LoggerFactory.getLogger(VulnContainerService.class);

    public String getUsernameFromToken(String token) {
        return jwtUtil.getUsernameFromToken(token);
    }

    public ContainerInstance createVulnEnvironment(
            User user, String imageName,
            Map<Integer, Integer> ports, int durationMinutes) {

        CreateContainerResponse response = null;
        try {
            ensureImageExists(imageName);

            Ports portBindings = new Ports();
            ports.forEach((hostPort, containerPort) ->
                    portBindings.bind(ExposedPort.tcp(containerPort),
                            Ports.Binding.bindPort(hostPort))
            );

            response = dockerClient.createContainerCmd(imageName)
                    .withExposedPorts(ports.values().stream()
                            .map(ExposedPort::tcp)
                            .collect(Collectors.toList()))
                    .withHostConfig(buildSecureHostConfig(portBindings))
                    .exec();

            // 启动容器时，如果遇到NotModifiedException，则认为容器已处于正确状态
            try {
                dockerClient.startContainerCmd(response.getId()).exec();
            } catch (NotModifiedException nme) {
                log.warn("容器 {} 已经处于预期状态，忽略启动异常: {}", response.getId(), nme.getMessage());
            }
            InspectContainerResponse inspect = dockerClient.inspectContainerCmd(response.getId()).exec();
            if (!Boolean.TRUE.equals(inspect.getState().getRunning())) {
                throw new RuntimeException("容器启动失败: " + inspect.getState().getStatus());
            }

            ContainerInstance instance = new ContainerInstance();
            instance.setUser(user);
            instance.setImageName(imageName);
            instance.setPortMapping(ports);
            instance.setContainerId(response.getId());
            instance.setCreateTime(Instant.now());
            instance.setExpireTime(Instant.now().plus(durationMinutes, ChronoUnit.MINUTES));
            instance.setStatus("RUNNING");

            ContainerInstance saved = mongoTemplate.save(instance);
            user.getActiveLabs().add(saved);
            mongoTemplate.save(user);

            return saved;

        } catch (Exception e) {
            if (response != null) {
                try {
                    dockerClient.removeContainerCmd(response.getId()).withForce(true).exec();
                } catch (Exception ignored) {}
            }
            throw new RuntimeException("创建容器失败: " + e.getMessage(), e);
        }
    }

    private String createNetwork(String networkName) {
        try {
            try {
                dockerClient.inspectNetworkCmd().withNetworkId(networkName).exec();
                return networkName;
            } catch (NotFoundException e) {
                CreateNetworkResponse response = dockerClient.createNetworkCmd()
                        .withName(networkName)
                        .withDriver("bridge")
                        .exec();
                return response.getId();
            }
        } catch (Exception e) {
            throw new RuntimeException("创建网络失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建组合环境（多容器编排）
     * @param user 用户对象
     * @param services 服务规格列表
     * @param durationMinutes 环境持续时间(分钟)
     * @return 创建的组合环境对象
     */
    public ComposeEnvironment createComposeEnvironment(
            User user, List<ServiceSpec> services,
            int durationMinutes) {

        // 生成唯一环境ID和动态数据库名称
        String envId = UUID.randomUUID().toString();
        String dynamicDbName = "db_" + user.getId() + "_" + envId.substring(0, 8);

        // ✅ 从 backend 服务中提取 env
        Map<String, String> backendEnv = services.stream()
                .filter(s -> s.getServiceName().toLowerCase().contains("backend"))
                .map(ServiceSpec::getEnv)
                .findFirst()
                .orElse(new HashMap<>());

        // 1. 动态配置服务环境变量
        configureServiceEnvironments(services, backendEnv);

        // 2. 创建隔离网络
        String networkName = String.format("user-%s-env-%s", user.getId(), envId);
        String networkId = createNetwork(networkName);

        // 3. 按依赖顺序启动服务
        Map<String, String> containerIds = new LinkedHashMap<>();
        try {
            // 3.1 首先创建所有容器但不启动
            createAllContainers(services, networkId, containerIds);

            // 3.2 按顺序启动服务并等待依赖就绪
            startServicesWithDependencies(services, containerIds);

            // 4. 保存环境记录到数据库
            ComposeEnvironment env = new ComposeEnvironment();
            env.setUser(user);
            env.setNetworkId(networkId);
            env.setServices(containerIds);
            env.setCreateTime(Instant.now());
            env.setExpireTime(Instant.now().plus(durationMinutes, ChronoUnit.MINUTES));
            env.setStatus("RUNNING");

            return mongoTemplate.save(env);

        } catch (Exception e) {
            // 清理资源
            containerIds.values().forEach(id -> {
                try {
                    dockerClient.removeContainerCmd(id).withForce(true).exec();
                } catch (Exception ex) {
                    log.error("清理容器 {} 失败: {}", id, ex.getMessage());
                }
            });
            try {
                dockerClient.removeNetworkCmd(networkId).exec();
            } catch (Exception ex) {
                log.error("清理网络 {} 失败: {}", networkId, ex.getMessage());
            }
            throw new RuntimeException("环境创建失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建所有容器但不启动
     * @param services 服务规格列表
     * @param networkId Docker网络ID
     * @param containerIds 用于存储容器ID的Map
     */
    private void createAllContainers(List<ServiceSpec> services,
                                     String networkId,
                                     Map<String, String> containerIds) throws InterruptedException {
        for (ServiceSpec spec : services) {
            try {
                log.info("正在创建容器: {}", spec.getServiceName());
                CreateContainerResponse response = createServiceContainer(spec, networkId, networkId);
                containerIds.put(spec.getServiceName(), response.getId());
                log.info("容器 {} 创建成功, ID: {}", spec.getServiceName(), response.getId());
            } catch (Exception e) {
                log.error("创建容器 {} 失败", spec.getServiceName(), e);
                throw e;
            }
        }
    }

    /**
     * 按依赖顺序启动服务
     * @param services 服务规格列表
     * @param containerIds 容器ID映射
     */
    private static final int MAX_BACKEND_WAIT_RETRIES = 5; // 最大等待次数
    private static final int BACKEND_WAIT_DELAY_MS = 2000;   // 每次等待2秒

    private void startServicesWithDependencies(List<ServiceSpec> services,
                                               Map<String, String> containerIds) throws InterruptedException {

        // 排序: 数据库 -> 后端 -> 前端
        List<ServiceSpec> orderedServices = services.stream()
                .sorted(Comparator.comparingInt(s ->
                        s.getImage().contains("mysql") ? 0 :
                                s.getImage().contains("backend") ? 1 : 2))
                .collect(Collectors.toList());

        for (ServiceSpec spec : orderedServices) {
            String containerId = containerIds.get(spec.getServiceName());
            log.info("正在启动服务: {}", spec.getServiceName());

            try {
                // 如果是前端，需要先检测 backend 是否就绪
                if (spec.getImage().contains("frontend")) {
                    String backendId = containerIds.get("backend");
                    boolean backendReady = false;
                    int retry = 0;
                    String backendName = services.stream()
                            .filter(s -> s.getServiceName().toLowerCase().contains("backend"))
                            .map(ServiceSpec::getServiceName)
                            .findFirst().orElse("backend");
                    while (retry < MAX_BACKEND_WAIT_RETRIES) {
                        if (isPortMapped(backendName, 3000)) {
                            backendReady = true;
                            break;
                        }
                        log.info("检测到 "+backendName+" 尚未就绪，等待 {} 毫秒...", BACKEND_WAIT_DELAY_MS);
                        Thread.sleep(BACKEND_WAIT_DELAY_MS);
                        retry++;
                    }
                    if (!backendReady) {
                        throw new RuntimeException("Backend not ready after waiting, frontend will crash");
                    }
                }

                // 启动容器（对 NotModifiedException 容错处理）
                try {
                    dockerClient.startContainerCmd(containerId).exec();
                    log.info("容器 {} 启动命令已发送", spec.getServiceName());
                } catch (NotModifiedException nme) {
                    log.warn("容器 {} 已经处于预期状态，忽略启动异常: {}", spec.getServiceName(), nme.getMessage());
                }

                // 对MySQL进行特殊处理 - 等待完全就绪
                if (spec.getImage().contains("mysql")) {
                    waitForMySQLReady(containerId);
                } else {
                    log.info("等待 {} 秒确保服务稳定...", SERVICE_START_DELAY);
                    Thread.sleep(SERVICE_START_DELAY * 1000);
                }

                // 检查容器状态
                Thread.sleep(2000); // 稍微等一下
                InspectContainerResponse inspect = dockerClient.inspectContainerCmd(containerId).exec();
                if (!inspect.getState().getRunning()) {
                    log.error("容器 {} 启动失败，当前状态: {}", containerId, inspect.getState().getStatus());
                    debugContainerLogs(containerId); // 打印关键日志
                    throw new RuntimeException("容器启动失败: " + inspect.getState().getStatus());
                }

                log.info("服务 {} 启动成功", spec.getServiceName());
            } catch (Exception e) {
                log.error("启动服务 {} 失败", spec.getServiceName(), e);
                debugContainerLogs(containerId);
                throw e;
            }
        }
    }


    /**
     * 等待MySQL容器完全就绪
     * @param containerId MySQL容器ID
     */
    private void waitForMySQLReady(String containerId) {
        log.info("Checking if MySQL container {} is running...", containerId);
        waitForContainerState(containerId, "running", 30);
        log.info("MySQL container {} is running. Waiting for it to be ready...", containerId);
        Instant start = Instant.now();
        Instant end = start.plusSeconds(MYSQL_WAIT_TIMEOUT);
        String password = getMySQLPassword(containerId);
        log.debug("MySQL root password for container {}: {}", containerId, password);

        while (Instant.now().isBefore(end)) {
            try {
                // 使用 bash 内执行 MySQL 测试连接
                String[] testCmd = {
                        "bash", "-c",
                        String.format("mysql -h 127.0.0.1 -uroot -p%s -e 'SELECT 1'", password)
                };
                String execId = dockerClient.execCreateCmd(containerId)
                        .withCmd(testCmd)
                        .withAttachStderr(true)
                        .withAttachStdout(true)
                        .exec().getId();
                LogCollectorCallback callback = new LogCollectorCallback();
                dockerClient.execStartCmd(execId)
                        .exec(callback)
                        .awaitCompletion(10, TimeUnit.SECONDS);
                int exitCode = dockerClient.inspectExecCmd(execId).exec().getExitCode();
                String logs = callback.getLogs();

                if (exitCode == 0) {
                    log.info("MySQL connection test successful (took {} seconds)",
                            ChronoUnit.SECONDS.between(start, Instant.now()));
                    return;
                }

                // 检查 mysqld 进程是否启动
                if (isMySQLProcessRunning(containerId)) {
                    log.info("MySQL process started (took {} seconds)",
                            ChronoUnit.SECONDS.between(start, Instant.now()));
                    return;
                }

                Thread.sleep(2000);
            } catch (Exception e) {
                log.debug("Health check exception: {}", e.getMessage());
            }
        }

        debugContainerLogs(containerId);
        throw new RuntimeException("MySQL startup timed out");
    }

    private void waitForContainerState(String containerId, String targetState, int timeoutSeconds) {
        Instant end = Instant.now().plusSeconds(timeoutSeconds);
        while (Instant.now().isBefore(end)) {
            InspectContainerResponse inspect = dockerClient.inspectContainerCmd(containerId).exec();
            if (targetState.equals(inspect.getState().getStatus())) {
                return;
            }
        }
        throw new RuntimeException("等待容器状态超时");
    }

    private boolean isMySQLProcessRunning(String containerId) {
        try {
            String execId = dockerClient.execCreateCmd(containerId)
                    .withCmd("pgrep", "mysqld")
                    .exec().getId();

            LogCollectorCallback callback = new LogCollectorCallback();
            dockerClient.execStartCmd(execId).exec(callback).awaitCompletion(2, TimeUnit.SECONDS);

            return dockerClient.inspectExecCmd(execId).exec().getExitCode() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    // 修改后的 isPortListening 方法
    private boolean isPortMapped(String containerId, int port) {
        try {
            InspectContainerResponse containerInfo = dockerClient.inspectContainerCmd(containerId).exec();
            Map<ExposedPort, Ports.Binding[]> portBindings = containerInfo.getNetworkSettings().getPorts().getBindings();

            if (portBindings != null) {
                for (Map.Entry<ExposedPort, Ports.Binding[]> entry : portBindings.entrySet()) {
                    ExposedPort exposedPort = entry.getKey();
                    Ports.Binding[] bindings = entry.getValue();

                    // 检查容器是否映射了指定端口，且有绑定主机端口
                    if (exposedPort.getPort() == port && bindings != null && bindings.length > 0) {
                        for (Ports.Binding binding : bindings) {
                            if (binding.getHostPortSpec() != null && !binding.getHostPortSpec().isEmpty()) {
                                log.debug("容器 {} 端口 {} 已映射到主机端口 {}", containerId, port, binding.getHostPortSpec());
                                return true;
                            }
                        }
                    }
                }
            }

            log.debug("容器 {} 未映射端口 {}", containerId, port);
            return false;
        } catch (Exception e) {
            log.error("检查容器端口映射失败: {}", e.getMessage());
            return false;
        }
    }

    private String getMySQLPassword(String containerId) {
        try {
            InspectContainerResponse inspect = dockerClient.inspectContainerCmd(containerId).exec();
            Optional<String> passwordVar = Arrays.stream(inspect.getConfig().getEnv())
                    .filter(env -> env.startsWith("MYSQL_ROOT_PASSWORD="))
                    .findFirst();

            if (passwordVar.isPresent()) {
                return passwordVar.get().split("=", 2)[1];
            } else {
                log.warn("未找到MYSQL_ROOT_PASSWORD环境变量，使用默认密码");
                return "root";
            }
        } catch (Exception e) {
            log.error("获取MySQL密码失败，使用默认密码", e);
            return "root";
        }
    }

    // 日志收集回调类
    private static class LogCollectorCallback extends LogContainerResultCallback {
        private final StringBuilder logs = new StringBuilder();

        @Override
        public void onNext(Frame frame) {
            logs.append(new String(frame.getPayload()));
            super.onNext(frame);
        }

        public String getLogs() {
            return logs.toString();
        }
    }

    /**
     * 创建服务容器
     * @param spec 服务规格
     * @param networkId 网络ID
     * @return 容器创建响应
     */
    private CreateContainerResponse createServiceContainer(ServiceSpec spec,
                                                           String networkId, String networkName) throws InterruptedException {
        // 确保镜像存在
        ensureImageExists(spec.getImage());

        // 端口绑定配置
        Ports portBindings = new Ports();
        spec.getPorts().forEach((hostPort, containerPort) ->
                portBindings.bind(ExposedPort.tcp(containerPort),
                        Ports.Binding.bindPort(hostPort))
        );

        // 环境变量配置
        List<String> envVars = spec.getEnv().entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.toList());

        // 创建容器
        return dockerClient.createContainerCmd(spec.getImage())
                .withName(spec.getServiceName())
                .withExposedPorts(spec.getPorts().values().stream()
                        .map(ExposedPort::tcp)
                        .collect(Collectors.toList()))
                .withHostConfig(buildSecureHostConfig(portBindings)
                        .withNetworkMode(networkName))
                .withEnv(envVars)
                .exec();
    }

    /**
     * 配置服务环境变量
     * @param services 服务列表
     * @param dbName 数据库名称
     */
    /**
     * 配置服务环境变量（使用前端传入 env 数据为主）
     */
    private void configureServiceEnvironments(List<ServiceSpec> services, Map<String, String> backendEnv) {
        String backendName = services.stream()
                .filter(s -> s.getServiceName().toLowerCase().contains("backend"))
                .map(ServiceSpec::getServiceName)
                .findFirst().orElse("backend");

        services.forEach(spec -> {
            String serviceName = spec.getServiceName().toLowerCase();

            // mysql 额外初始化参数
            if (serviceName.contains("mysql")) {
                spec.getEnv().putIfAbsent("MYSQL_DEFAULT_AUTHENTICATION_PLUGIN", "mysql_native_password");
                spec.getEnv().putIfAbsent("MYSQL_INITDB_SKIP_TZINFO", "1");
                spec.getEnv().putIfAbsent("MYSQL_DISABLE_FAST_SHUTDOWN", "1");
                spec.getEnv().putIfAbsent("MYSQL_INITDB_WAIT_TIMEOUT", "30");
            }

            // backend 设置数据库连接参数（从前端 env 中动态读取）
            if (serviceName.contains("backend")) {
                spec.getEnv().put("DB_HOST", backendEnv.getOrDefault("DB_HOST", "mysql"));
                spec.getEnv().put("DB_PORT", backendEnv.getOrDefault("DB_PORT", "3306"));
                spec.getEnv().put("DB_NAME", backendEnv.getOrDefault("DB_NAME", "vulnerable_db"));
                spec.getEnv().put("DB_USER", backendEnv.getOrDefault("DB_USER", "root"));
                spec.getEnv().put("DB_PASSWORD", backendEnv.getOrDefault("DB_PASSWORD", "123456"));
                spec.getEnv().put("JDBC_PARAMS", backendEnv.getOrDefault("JDBC_PARAMS", "useSSL=false&allowPublicKeyRetrieval=true"));
            }

            // frontend 设置后台地址（确保能访问后端容器）
            if (serviceName.contains("frontend")) {
                String backendUrl = "http://" + backendName + ":3000";
                spec.getEnv().put("VITE_BACKEND_NAME", backendName);
                spec.getEnv().put("NGINX_BACKEND_URL", backendUrl);
                spec.getEnv().put("NGINX_UPSTREAM", backendName);
            }
        });
    }

    /**
     * 容器监控任务（定时执行）
     */
    @Scheduled(fixedRate = MONITOR_INTERVAL)
    public void monitorContainers() {
        try {
            log.info("开始容器健康检查...");

            monitorSingleContainers();
            monitorComposeEnvironments();

            log.info("容器健康检查完成");
        } catch (Exception e) {
            log.error("容器监控任务执行失败", e);
        }
    }

    /**
     * 监控独立容器状态
     */
    private void monitorSingleContainers() {
        mongoTemplate.find(
                        Query.query(Criteria.where("status").is("RUNNING")),
                        ContainerInstance.class)
                .forEach(instance -> {
                    String containerId = instance.getContainerId();
                    try {
                        if (!isContainerExists(containerId)) {
                            log.warn("检测到幽灵容器记录: {}", containerId);
                            forceDestroyContainer(instance);
                            return;
                        }

                        InspectContainerResponse inspect = dockerClient.inspectContainerCmd(containerId).exec();
                        if (!inspect.getState().getRunning()) {
                            log.warn("容器 {} 异常停止，状态: {}", containerId, inspect.getState().getStatus());
                            debugContainerLogs(containerId);
                            handleStoppedContainer(instance, inspect.getState().getStatus());
                        }
                    } catch (Exception e) {
                        log.error("监控容器 {} 异常", containerId, e);
                    }
                });
    }

    /**
     * 监控组合环境状态
     */
    private void monitorComposeEnvironments() {
        mongoTemplate.find(
                        Query.query(Criteria.where("status").is("RUNNING")),
                        ComposeEnvironment.class)
                .forEach(env -> {
                    int before = env.getServices().size();
                    env.getServices().entrySet().removeIf(entry ->
                            !isContainerExists(entry.getValue()));

                    if (env.getServices().size() != before) {
                        mongoTemplate.save(env);
                        log.info("更新组合环境 {} 容器列表", env.getId());
                    }
                });
    }

    /**
     * 处理停止的容器
     * @param instance 容器实例
     * @param reason 停止原因
     */
    private void handleStoppedContainer(ContainerInstance instance, String reason) {
        String containerId = instance.getContainerId();
        log.warn("处理停止容器: {}, 原因: {}", containerId, reason);

        if (!isContainerExists(containerId)) {
            forceDestroyContainer(instance);
            return;
        }

        if (instance.getRestartCount() < MAX_RESTART_ATTEMPTS) {
            try {
                try {
                    dockerClient.startContainerCmd(containerId).exec();
                    log.info("容器 {} 重启命令已发送", containerId);
                } catch (NotModifiedException nme) {
                    log.warn("容器 {} 已经在预期状态，忽略启动异常: {}", containerId, nme.getMessage());
                }
                instance.setRestartCount(instance.getRestartCount() + 1);
                mongoTemplate.save(instance);
                log.info("容器 {} 重启成功 (第 {} 次)", containerId, instance.getRestartCount());
            } catch (Exception e) {
                log.error("重启容器 {} 失败", containerId, e);
                forceDestroyContainer(instance);
            }
        } else {
            log.warn("容器 {} 达到最大重启次数，强制销毁", containerId);
            forceDestroyContainer(instance);
        }
    }

    /**
     * 收集容器日志用于调试
     * @param containerId 容器ID
     */
    private void debugContainerLogs(String containerId) {
        if (!isContainerExists(containerId)) {
            log.warn("容器 {} 不存在，无法收集日志", containerId);
            return;
        }

        try {
            StringBuilder logs = new StringBuilder();
            int sinceSeconds = (int) Instant.now().minus(5, ChronoUnit.MINUTES).getEpochSecond();

            dockerClient.logContainerCmd(containerId)
                    .withStdOut(true).withStdErr(true)
                    .withTail(100).withSince(sinceSeconds)
                    .exec(new LogContainerResultCallback() {
                        @Override
                        public void onNext(Frame frame) {
                            logs.append(new String(frame.getPayload())).append("\n");
                        }
                    }).awaitCompletion(10, TimeUnit.SECONDS);

            if (logs.length() > 0) {
                log.info("容器 {} 日志:\n{}", containerId, logs);
            }
        } catch (Exception e) {
            log.error("收集容器 {} 日志失败", containerId, e);
        }
    }

    /**
     * 强制销毁容器
     * @param instance 容器实例
     */
    private void forceDestroyContainer(ContainerInstance instance) {
        if (instance == null || instance.getContainerId() == null) {
            log.error("无效的容器实例");
            return;
        }

        String containerId = instance.getContainerId();
        try {
            try {
                dockerClient.stopContainerCmd(containerId)
                        .withTimeout(10).exec();
            } catch (NotFoundException e) {
                log.warn("容器 {} 已不存在", containerId);
            }

            dockerClient.removeContainerCmd(containerId)
                    .withForce(true).exec();

            instance.setStatus("DESTROYED");
            instance.setExpireTime(Instant.now());
            mongoTemplate.save(instance);

            if (instance.getUser() != null) {
                User user = instance.getUser();
                user.getActiveLabs().removeIf(lab ->
                        lab instanceof ContainerInstance &&
                                containerId.equals(((ContainerInstance) lab).getContainerId()));
                mongoTemplate.save(user);
            }

            log.info("容器 {} 销毁完成", containerId);
        } catch (Exception e) {
            log.error("销毁容器 {} 失败", containerId, e);
        }
    }

    /**
     * 检查容器是否存在
     * @param containerId 容器ID
     * @return 容器是否存在
     */
    private boolean isContainerExists(String containerId) {
        try {
            dockerClient.inspectContainerCmd(containerId).exec();
            return true;
        } catch (NotFoundException e) {
            return false;
        } catch (Exception e) {
            log.error("检查容器存在性异常: {}", containerId, e);
            return false;
        }
    }

    /**
     * 确保镜像存在，不存在则拉取
     * @param imageName 镜像名称
     */
    private void ensureImageExists(String imageName) throws InterruptedException, DockerException {
        try {
            dockerClient.inspectImageCmd(imageName).exec();
        } catch (NotFoundException e) {
            dockerClient.pullImageCmd(imageName)
                    .start()
                    .awaitCompletion(5, TimeUnit.MINUTES);
        }
    }

    /**
     * 构建安全的主机配置
     * @param portBindings 端口绑定配置
     * @return 主机配置
     */
    private HostConfig buildSecureHostConfig(Ports portBindings) {
        return HostConfig.newHostConfig()
                .withPortBindings(portBindings)
                .withPrivileged(true)
                .withReadonlyRootfs(false)
                .withMemory(512 * 1024 * 1024L) // 内存限制512MB
                .withCpuShares(512) // CPU份额
                .withPidsLimit(100L) // 进程数限制
                .withRestartPolicy(RestartPolicy.onFailureRestart(3)) // 失败时重启3次
                .withCapAdd(Capability.NET_ADMIN, Capability.SYS_ADMIN); // 添加权限
    }

    /**
     * 服务规格定义类
     */
    @Data
    public static class ServiceSpec {
        private String serviceName;
        private String image;
        private Map<Integer, Integer> ports = new HashMap<>();
        private Map<String, String> env = new HashMap<>();
        private List<String> dependsOn = new ArrayList<>(); // 显式声明依赖
    }
}
