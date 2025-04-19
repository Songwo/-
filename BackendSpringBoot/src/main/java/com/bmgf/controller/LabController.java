package com.bmgf.controller;

import com.bmgf.dao.ChallengeRepository;
import com.bmgf.po.*;
import com.bmgf.service.impl.*;
import com.bmgf.DTO.FlagDTo;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/lab")
@RequiredArgsConstructor
public class LabController {
    private final VulnContainerService containerService;
    private final UserService userService;

    @Autowired
    private ContainerInstanceService containerInstanceService;

    @Autowired
    private LabFlagService labFlagService;

    @PostMapping("/create")
    public ResponseEntity<?> createLab(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody LabCreateRequest request) {

        try {
            // 1. 从Authorization头获取token并解析用户名
            String token = authHeader.substring(7); // 去掉"Bearer "前缀
            String username = containerService.getUsernameFromToken(token);

            // 2. 获取用户信息
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(errorResponse("Authentication failed", "User not found"));
            }

            // 3. 验证请求参数
            if (request.getImageName() == null || request.getImageName().isBlank()) {
                throw new IllegalArgumentException("Image name is required");
            }
            if (request.getPorts() == null || request.getPorts().isEmpty()) {
                throw new IllegalArgumentException("At least one port mapping is required");
            }

            // 4. 创建容器（同步调用）
            ContainerInstance instance = containerService.createVulnEnvironment(
                    user,
                    request.getImageName(),
                    request.getPorts(),
                    request.getDuration()
            );

            return ResponseEntity.ok(convertToDTO(instance));

        } catch (IllegalArgumentException e) {
            log.warn("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(errorResponse("Invalid request", e.getMessage()));
        } catch (Exception e) {
            log.error("Container creation failed", e);
            return ResponseEntity.internalServerError()
                    .body(errorResponse("Container creation failed",
                            e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
        }
    }

    private ContainerInstanceDTO convertToDTO(ContainerInstance instance) {
        ContainerInstanceDTO dto = new ContainerInstanceDTO();
        dto.setContainerId(instance.getContainerId());
        dto.setImageName(instance.getImageName());
        dto.setPortMapping(instance.getPortMapping());
        dto.setExpireTime(instance.getExpireTime());
        dto.setStatus(instance.getStatus());
        return dto;
    }

    private Map<String, Object> errorResponse(String error, String message) {
        return Map.of(
                "error", error,
                "message", message,
                "timestamp", Instant.now()
        );
    }

    @PostMapping("/create-compose")
    public ResponseEntity<?> createComposeLab(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ComposeCreateRequest request) {

        try {
            // 身份验证
            String token = authHeader.substring(7);
            String username = containerService.getUsernameFromToken(token);
            User user = userService.findByUsername(username);

            // 参数校验
            if (request.getServices() == null || request.getServices().isEmpty()) {
                throw new IllegalArgumentException("At least one service is required");
            }

            // 创建服务依赖图
            Map<String, ServiceDefinition> serviceMap = request.getServices().stream()
                    .collect(Collectors.toMap(ServiceDefinition::getServiceName, s -> s));

            // 获取服务启动顺序
            List<ServiceDefinition> orderedServices = resolveServiceDependencies(serviceMap);

            // 转换请求参数
            List<VulnContainerService.ServiceSpec> specs = orderedServices.stream()
                    .map(s -> {
                        VulnContainerService.ServiceSpec spec = new VulnContainerService.ServiceSpec();
                        spec.setServiceName(s.getServiceName());
                        spec.setImage(s.getImage());
                        spec.setPorts(s.getPorts());
                        spec.setEnv(s.getEnv());
                        return spec;
                    })
                    .collect(Collectors.toList());

            // 创建环境
            ComposeEnvironment env = containerService.createComposeEnvironment(
                    user, specs, request.getDuration());

            return ResponseEntity.ok(convertComposeToDTO(env));

        } catch (IllegalArgumentException e) {
            log.warn("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(errorResponse("Invalid request", e.getMessage()));
        } catch (Exception e) {
            log.error("Container creation failed", e);
            return ResponseEntity.internalServerError()
                    .body(errorResponse("Container creation failed",
                            e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
        }
    }

    @PostMapping("/flag_2")
    public Result flagLab_2(@RequestBody FlagDTo flagDTo, @RequestHeader("Authorization") String authHeader){
        String token = authHeader.substring(7);
        String username = containerService.getUsernameFromToken(token);
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error("没有权限");
        }
        if(containerInstanceService.checkIfUserCreatedContainer(flagDTo.getUserId(),flagDTo.getImageName())){
                System.out.println("Extracted content: " + flagDTo.getFlag());
                return labFlagService.checkFlag(flagDTo.getImageName(), flagDTo.getFlag())?Result.success():Result.error("回答错误");
        }else {
            return Result.error("没有容器创建记录");
        }
    }
    private final ComposeEnvironmentService composeEnvironmentService;
    @Autowired
    private ChallengeService challengeService;
    @Autowired
    private User_Challenge_ProgressService userChallengeProgressService;
    @PostMapping("/flag")
    public Result flagLab(@RequestBody FlagDTo flagDTo, @RequestHeader("Authorization") String authHeader){
        String token = authHeader.substring(7);
        String username = containerService.getUsernameFromToken(token);
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error("没有权限");
        }
        if(composeEnvironmentService.checkIfUserCreatedImage(flagDTo.getUserId(),flagDTo.getImageName())){
            boolean isCorrect = labFlagService.checkFlag(flagDTo.getImageName(), flagDTo.getFlag());
            if (isCorrect) {
                Optional<Challenge> challengeOpt = challengeService.findByImageName(flagDTo.getImageName());
                if (challengeOpt.isPresent()) {
                    String challengeId = challengeOpt.get().getId();
                    String nextChallengeId = getNextChallengeId(challengeId);
                    userChallengeProgressService.completeChallenge(user.getId(), challengeId, 100); // 100为分数
                    if (nextChallengeId != null) {
                        challengeService.unlockNextChallenge(user.getId(), nextChallengeId);
                    }
                }
                return Result.success();
            } else {
                return Result.error("回答错误");
            }
        } else {
            return Result.error("没有容器创建记录");
        }
    }

    @Autowired
    private ChallengeRepository challengeRepository;

    public String getNextChallengeId(String currentChallengeId) {
        List<Challenge> all = challengeRepository.findAll();
        all.sort(Comparator.comparingInt(Challenge::getDifficulty).thenComparing(Challenge::getId));
        all.forEach(ch -> System.out.println(ch.getId() + " difficulty=" + ch.getDifficulty()));
        // 找到当前关卡
        int idx = -1;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId().equals(currentChallengeId)) {
                idx = i;
                break;
            }
        }
        if (idx == -1) return null;

        Challenge current = all.get(idx);
        // 先尝试解锁同难度下一个
        for (int i = idx + 1; i < all.size(); i++) {
            if (all.get(i).getDifficulty() == current.getDifficulty()) {
                System.out.println(all.get(i).getId() + " difficulty=" + all.get(i).getDifficulty());
                return all.get(i).getId();
            }
        }
        // 如果本难度已通关，解锁下一个难度的第一关
        for (int i = idx + 1; i < all.size(); i++) {
            if (all.get(i).getDifficulty() > current.getDifficulty()) {
                return all.get(i).getId();
            }
        }
        return null;
    }

    // 新增DTO转换方法
    private ComposeEnvironmentDTO convertComposeToDTO(ComposeEnvironment env) {
        ComposeEnvironmentDTO dto = new ComposeEnvironmentDTO();
        dto.setEnvironmentId(env.getId());
        dto.setServices(env.getServices().keySet());
        dto.setExpireTime(env.getExpireTime());
        dto.setStatus(env.getStatus());
        return dto;
    }

    // 新增请求响应类
    @Data
    public static class ComposeCreateRequest {
        private List<ServiceDefinition> services;
        private int duration = 60;
    }

    @Data
    public static class ServiceDefinition {
        private String serviceName;
        private String image;
        private Map<Integer, Integer> ports;
        private Map<String, String> env = new HashMap<>();
        private List<String> dependsOn = new ArrayList<>();  // 新增字段：依赖关系
    }

    @Data
    public static class ComposeEnvironmentDTO {
        private String environmentId;
        private Set<String> services;
        private Instant expireTime;
        private String status;
    }

    @Data
    public static class LabCreateRequest {
        private String imageName;
        private Map<Integer, Integer> ports;
        private int duration = 60;
    }

    @Data
    public static class ContainerInstanceDTO {
        private String containerId;
        private String imageName;
        private Map<Integer, Integer> portMapping;
        private Instant expireTime;
        private String status;
    }

    private List<ServiceDefinition> resolveServiceDependencies(Map<String, ServiceDefinition> serviceMap) {
        List<ServiceDefinition> orderedServices = new ArrayList<>();
        Set<String> processed = new HashSet<>();

        // 递归地处理服务依赖关系
        for (ServiceDefinition service : serviceMap.values()) {
            processServiceDependencies(service, serviceMap, orderedServices, processed);
        }

        return orderedServices;
    }

    private void processServiceDependencies(ServiceDefinition service,
                                            Map<String, ServiceDefinition> serviceMap,
                                            List<ServiceDefinition> orderedServices,
                                            Set<String> processed) {
        // 防止循环依赖
        if (processed.contains(service.getServiceName())) {
            return;
        }
        processed.add(service.getServiceName());

        // 递归处理所有依赖的服务
        for (String dependency : service.getDependsOn()) {
            ServiceDefinition dependentService = serviceMap.get(dependency);
            if (dependentService != null) {
                processServiceDependencies(dependentService, serviceMap, orderedServices, processed);
            }
        }

        // 将当前服务添加到列表中
        orderedServices.add(service);
    }
}
