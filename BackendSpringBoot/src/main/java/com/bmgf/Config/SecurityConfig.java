package com.bmgf.Config;
import com.bmgf.util.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. CORS配置
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. 禁用CSRF（因使用JWT）
                .csrf(AbstractHttpConfigurer::disable)

                // 3. 会话管理（无状态）
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. 授权配置
                .authorizeHttpRequests(auth -> auth
                        // 公开路径
                        .requestMatchers(
                                // 用户认证相关
                                "/user/login",
                                "/user/register",
                                "/admin/login",
                                "/error",

                                // 静态资源访问
                                "/avatar/**",
                                "/images/**",
                                "/video/**",
                                "/upload/**",

                                // 社区论坛相关
                                "/post/findAll",
                                "/post/findBySection",
                                "/post/findById",
                                "/post/search",
                                "/post/hot",
                                "/comments/find/**",

                                // 漏洞库相关
                                "/user/findAllHole",
                                "/bug/findById/**",
                                "/bug/search",

                                // 其他公开API
                                "/api/public/**"
                        ).permitAll()

                        // 特定权限要求
                        // 放行所有OPTIONS请求
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // 其他请求需要认证
                        .anyRequest().authenticated()
                )

                // 5. 用户详情服务
                .userDetailsService(userDetailsService)

                // 6. 添加JWT过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // CORS配置（保持原样）
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(
                "https://54.153.18.78",
                "https://netkpi.icu",
                "http://localhost:8080",
                "http://localhost:3000",
                "http://localhost:5173",
                "http://47.117.70.79",
                "https://www.wacyg.fun",
                "http://www.wacyg.fun",
                "https://wacyg.fun",
                "http://wacyg.fun"
        ));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With",
                "Accept","X-User-ID"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}