package com.itheima.csstudent.controller;
import com.itheima.csstudent.DTO.FlagDTo;
import com.itheima.csstudent.po.*;
import com.itheima.csstudent.service.impl.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/lab")
@RequiredArgsConstructor
public class LabController {
    private final VulnContainerService containerService;
    private final UserService userService;

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
    // LabController.java 新增端点
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

            // 转换请求参数
            List<VulnContainerService.ServiceSpec> specs = request.getServices().stream()
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

    @Autowired
    private ContainerInstanceService containerInstanceService;
    private LabFlagService labFlagService;
    @PostMapping("/flag")
    public com.itheima.csstudent.po.Result flagLab(@RequestBody FlagDTo flagDTo, @RequestHeader("Authorization") String authHeader){
        String token = authHeader.substring(7);
        String username = containerService.getUsernameFromToken(token);
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error("没有权限");
        }
        if(containerInstanceService.checkIfUserCreatedContainer(flagDTo.getUserId(),flagDTo.getImageName())){
            String regex = "\\{(.*?)\\}";
            Pattern pattern = Pattern.compile(regex);

            // 创建Matcher对象
            Matcher matcher = pattern.matcher(flagDTo.getFlag());

            // 查找并提取匹配的内容
            if (matcher.find()) {
                String extracted = matcher.group(1); // 获取第一个分组，即大括号内的内容
                System.out.println("Extracted content: " + extracted);
                return labFlagService.checkFlag(flagDTo.getImageName(), extracted)?Result.success():Result.error("回答错误");
            } else {
                System.out.println("No match found");
                return Result.error("答案为空");
            }

        }else {
            return Result.error("没有容器创建记录");
        }


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
}