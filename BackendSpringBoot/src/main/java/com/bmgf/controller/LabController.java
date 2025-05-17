package com.bmgf.controller;
import com.bmgf.DTO.SharedEnvRequest;
import com.bmgf.dao.ChallengeRepository;
import com.bmgf.po.*;
import com.bmgf.service.impl.*;
import com.bmgf.DTO.FlagDTo;
import com.bmgf.util.JwtUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
@Slf4j
@RestController
@RequestMapping("/lab")
@RequiredArgsConstructor
public class LabController {


    private final VulnContainerService containerService;
    private final UserService userService;
    @Autowired
    private  JwtUtil jwtUtil;
    @Autowired
    private LabFlagService labFlagService;
    @Autowired
    private ContainerService containerService1;

    @PostMapping("/shared")
    public ResponseEntity<?> createSharedEnv(@RequestBody SharedEnvRequest request) {
        try {
            ContainerInstance instance = containerService1.createSharedEnvironment(
                    request.getUserId(), request.getVulnType(), request.getDurationMinutes());
            return ResponseEntity.ok(instance);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // 异步组合环境创建接口
    @PostMapping("/create-compose")
    public CompletableFuture<ResponseEntity<?>> createComposeLabAsync(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ComposeCreateRequest request) {

        // 提取 token
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : null;
        if (token == null) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(errorResponse("Authentication failed", "Authorization header is missing or malformed"))
            );
        }

        // 验证 token
        if (!jwtUtil.validateToken(token)) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(errorResponse("Authentication failed", "Invalid or expired token"))
            );
        }
        // 获取用户名
        String username;
        try {
            username = containerService.getUsernameFromToken(token);
        } catch (Exception e) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(errorResponse("Authentication failed", "Token parsing failed"))
            );
        }
        // 查找用户
        User user = userService.findByUsername(username);
        if (user == null) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(errorResponse("Authentication failed", "User not found"))
            );
        }
        // 验证请求
        if (request.getServices() == null || request.getServices().isEmpty()) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.badRequest()
                            .body(errorResponse("Invalid request", "At least one service is required"))
            );
        }


        // 构建有序服务规格
        Map<String, ServiceDefinition> serviceMap = request.getServices().stream()
                .collect(Collectors.toMap(ServiceDefinition::getServiceName, s -> s));
        List<ServiceDefinition> orderedDefs = resolveServiceDependencies(serviceMap);
        List<VulnContainerService.ServiceSpec> specs = orderedDefs.stream()
                .map(s -> {
                    VulnContainerService.ServiceSpec spec = new VulnContainerService.ServiceSpec();
                    spec.setServiceName(s.getServiceName());
                    spec.setImage(s.getImage());
                    spec.setPorts(s.getPorts());
                    spec.setEnv(s.getEnv());
                    return spec;
                })
                .collect(Collectors.toList());

        // 异步创建组合环境
        return containerService.createComposeEnvironmentAsync(user, specs, request.getDuration())
                .<ResponseEntity<?>>thenApply(env -> {
                    ComposeEnvironmentDTO dto = convertComposeToDTO(env);
                    return ResponseEntity.ok(dto);
                })
                .exceptionally(ex -> {
                    Throwable cause = ex instanceof RuntimeException ? ex.getCause() : ex;
                    log.error("Compose creation failed", cause);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(errorResponse("Compose creation failed", cause.getMessage()));
                });
    }

    // Flag 接口
    @PostMapping("/flag_2")
    public Result flagLab2(@RequestBody FlagDTo flagDTo,
                           @RequestHeader("Authorization") String authHeader) {
        return labFlagService.checkFlag(flagDTo.getImageName(), flagDTo.getFlag())
                ? Result.success() : Result.error("没有容器创建记录");
    }

    @PostMapping("/flag")
    public Result flagLab(@RequestBody FlagDTo flagDTo,
                          @RequestHeader("Authorization") String authHeader) {
        return Result.success();
    }

    // DTO 转换 & 辅助方法
    private ComposeEnvironmentDTO convertComposeToDTO(ComposeEnvironment env) {
        ComposeEnvironmentDTO dto = new ComposeEnvironmentDTO();
        dto.setEnvironmentId(env.getId());
        dto.setServices(env.getServices().keySet());
        dto.setExpireTime(env.getExpireTime());
        dto.setStatus(env.getStatus());
        return dto;
    }

    private Map<String, Object> errorResponse(String error, String message) {
        return Map.of(
                "error", error,
                "message", message,
                "timestamp", Instant.now()
        );
    }

    private List<ServiceDefinition> resolveServiceDependencies(Map<String, ServiceDefinition> serviceMap) {
        List<ServiceDefinition> ordered = new ArrayList<>();
        Set<String> processed = new HashSet<>();
        for (ServiceDefinition svc : serviceMap.values()) {
            processServiceDependencies(svc, serviceMap, ordered, processed);
        }
        return ordered;
    }

    private void processServiceDependencies(ServiceDefinition service,
                                            Map<String, ServiceDefinition> serviceMap,
                                            List<ServiceDefinition> ordered,
                                            Set<String> processed) {
        if (!processed.add(service.getServiceName())) return;
        for (String dep : service.getDependsOn()) {
            ServiceDefinition d = serviceMap.get(dep);
            if (d != null) processServiceDependencies(d, serviceMap, ordered, processed);
        }
        ordered.add(service);
    }

    // 请求/响应 DTO
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
        private List<String> dependsOn = new ArrayList<>();
    }

    @Data
    public static class ComposeEnvironmentDTO {
        private String environmentId;
        private Set<String> services;
        private Instant expireTime;
        private String status;
    }
}