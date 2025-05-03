# 白帽工坊（BaiMaoGongFang）- 全栈网络安全学习平台

## 专栏分类

本项目在CSDN上的文章将按以下分类进行组织：

### 1. 项目概述
- [项目介绍与架构设计](http://wacyg.fun)
- [技术栈选型与对比](http://wacyg.fun)
- [项目特色与创新点](http://wacyg.fun)

### 2. 前端开发
- [Vue3 + TypeScript项目搭建](http://wacyg.fun)
- [Element Plus组件库实践](http://wacyg.fun)
- [前端安全防护实现](http://wacyg.fun)
- [响应式布局与移动端适配](http://wacyg.fun)

### 3. 后端开发
- [SpringBoot安全框架搭建](http://wacyg.fun)
- [JWT认证与授权实现](http://wacyg.fun)
- [Flask AI服务开发](http://wacyg.fun)
- [MongoDB数据模型设计](http://wacyg.fun)

### 4. CTF靶场
- [Docker靶场环境搭建](http://wacyg.fun)
- [Web漏洞环境配置](http://wacyg.fun)
- [Flag验证系统实现](http://wacyg.fun)
- [靶场安全防护措施](http://wacyg.fun)

### 5. 移动端开发
- [微信小程序开发实践](http://wacyg.fun)
- [Android App开发指南](http://wacyg.fun)
- [移动端安全防护](http://wacyg.fun)

### 6. 部署运维
- [Docker容器化部署](http://wacyg.fun)
- [Nginx配置与优化](http://wacyg.fun)
- [服务器安全加固](http://wacyg.fun)
- [性能优化实践](http://wacyg.fun)

### 7. 安全实践
- [Web安全防护](http://wacyg.fun)
- [API接口安全](http://wacyg.fun)
- [数据加密传输](http://wacyg.fun)
- [安全审计日志](http://wacyg.fun)

### 8. 开发技巧
- [代码规范与最佳实践](http://wacyg.fun)
- [调试与问题排查](http://wacyg.fun)
- [性能优化技巧](http://wacyg.fun)
- [开发工具使用](http://wacyg.fun)

### 9. 项目实战
- [功能模块开发实录](http://wacyg.fun)
- [问题解决方案](http://wacyg.fun)
- [性能优化案例](http://wacyg.fun)
- [安全加固实践](http://wacyg.fun)

### 10. 项目总结
- [开发经验总结](http://wacyg.fun)
- [技术难点突破](http://wacyg.fun)
- [项目优化方向](http://wacyg.fun)
- [未来发展规划](http://wacyg.fun)

## 项目摘要

白帽工坊是一个面向网络安全学习者的综合性平台，采用前后端分离架构，支持多端访问。项目集成了CTF靶场、在线课程、安全工具、AI问答等核心功能，为网络安全爱好者提供一站式的学习和实践环境。

**技术特点：**
- 前端：Vue3 + TypeScript + Element Plus
- 后端：SpringBoot + Flask + MongoDB + Redis
- 移动端：微信小程序 + Android App
- 特色：Docker动态靶场 + AI智能问答

**核心功能：**
- 实战CTF靶场：支持多种漏洞类型，实时flag验证
- 系统化课程：专业课程体系，视频教程，在线答疑
- 安全工具集：CMS识别、编码转换、端口扫描等
- AI智能问答：24/7在线解答，多轮对话
- 社区互动：技术论坛，经验分享，积分奖励

**项目亮点：**
1. 全栈技术栈，展示现代Web开发最佳实践
2. 多端适配，提供完整的用户体验
3. 安全性考虑，包含完整的认证授权机制
4. 可扩展架构，支持功能模块化扩展
5. 完整的部署文档，便于二次开发

## 项目简介

白帽工坊是一个综合性的网络安全学习平台，集成了Web前端、后端服务、微信小程序和安卓App，为网络安全爱好者提供一站式的学习与实践环境。项目采用现代化的技术栈，支持多端访问，已稳定上线运行。

本项目的开发目标是为网络安全爱好者、CTF选手和信息安全专业学生提供一个完整的学习和实践平台。通过整合理论学习、实战演练、社区交流和AI辅导等功能，帮助用户系统地掌握网络安全知识和技能。

## 功能亮点

### 1. 实战CTF靶场
- 基于Docker动态创建独立的攻防环境
- 支持多种漏洞类型：Web渗透、逆向工程、密码学等
- 实时flag验证与积分系统
- 靶场难度分级，适合不同水平用户

### 2. 系统化课程
- 专业的网络安全课程体系
- 高质量视频教程
- 在线答疑与辅导
- 学习进度追踪

### 3. 安全工具集
- CMS指纹识别
- Base64编解码工具
- IP地址查询
- 哈希生成器
- 密码强度检测
- 端口扫描工具

### 4. AI智能问答
- 基于大语言模型的智能答疑
- 24/7在线解答
- 上下文理解与多轮对话
- 专业知识库支持

### 5. 社区互动
- 技术论坛
- 经验分享
- 问题讨论
- 积分奖励机制
- 用户排行榜

### 6. 多端适配
- 响应式Web界面
- 微信小程序
- 安卓App
- 数据同步

### 7. 后台管理
- 用户管理
- 内容管理
- 视频管理
- 漏洞库管理
- 运营数据统计

## 技术栈详解

### 1. 前端技术
```typescript
// main.ts - Vue3项目配置示例
import { createApp } from 'vue'
import App from './App.vue'
import store from './store'
// 引入 Element Plus
import ElementPlus from 'element-plus'
// 引入 Element Plus 样式
import 'element-plus/dist/index.css'
import * as ElIcons from '@element-plus/icons-vue';
//引入路由器
import router from './router';
// 添加 Vuetify 样式
import vuetify from './plugins/vuetify.ts'
import 'vuetify/styles'

const app = createApp(App)
app.use(router)
app.use(store)
app.use(ElementPlus)
app.use(vuetify)
app.mount('#app')
```

- **Vue3**: 采用Composition API，提供更好的代码组织和复用
- **Element Plus & Vuetify**: 现代化UI组件库，提供丰富的界面元素
- **Vuex**: 状态管理，处理复杂的数据流
- **Vue Router**: 路由管理，实现SPA应用
- **Echarts**: 数据可视化，展示统计信息
- **TypeScript**: 类型安全，提高代码质量

### 2. 后端技术

#### SpringBoot后端
```java
// SecurityConfig.java - 安全配置示例
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/user/login", "/user/register").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, 
                UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```

- **SpringBoot**: 主要业务逻辑处理
- **Spring Security**: 认证和授权
- **JWT**: 无状态会话管理
- **MyBatis**: 数据库操作
- **Docker**: CTF靶场环境管理

#### Flask后端
```python
# app.py - Flask应用配置示例
from flask import Flask
from flask_cors import CORS
from routes.chat import chat_bp

app = Flask(__name__)
app.config.from_object(Config)

# CORS配置
CORS(app, resources={r"/api/*": {"origins": Config.ALLOWED_ORIGINS}})

# 注册路由
app.register_blueprint(chat_bp)

if __name__ == "__main__":
    app.run(host=Config.FLASK_HOST, port=Config.FLASK_PORT)
```

- **Flask**: AI问答服务
- **OpenAI API**: 智能对话支持
- **MongoDB**: 对话历史存储
- **Redis**: 缓存和速率限制

### 3. 数据库设计

#### MongoDB集合设计
```javascript
// 用户集合
{
  _id: ObjectId,
  username: String,
  password: String(hashed),
  email: String,
  role: String,
  points: Number,
  created_at: Date,
  avatar: String,
  last_login: Date
}

// CTF题目集合
{
  _id: ObjectId,
  title: String,
  description: String,
  difficulty: Number,
  category: String,
  flag: String(encrypted),
  points: Number,
  hints: [String],
  created_at: Date,
  solved_count: Number
}

// 论坛帖子集合
{
  _id: ObjectId,
  title: String,
  content: String,
  author: {
    id: ObjectId,
    username: String
  },
  tags: [String],
  created_at: Date,
  updated_at: Date,
  view_count: Number,
  like_count: Number,
  comment_count: Number
}
```

#### Redis使用
- 会话管理
- 验证码存储
- 接口限流
- 排行榜缓存

## 项目结构

```
/
├── Frontend/                # Vue3前端源码
│   ├── src/
│   │   ├── components/     # 通用组件
│   │   ├── views/         # 页面组件
│   │   ├── store/         # Vuex存储
│   │   ├── router/        # 路由配置
│   │   └── api/           # API接口
│   
├── BackendSpringBoot/      # SpringBoot后端
│   ├── src/main/java/
│   │   ├── controller/    # 接口控制器
│   │   ├── service/       # 业务逻辑
│   │   ├── model/         # 数据模型
│   │   └── config/        # 配置类
│   
├── BackendFlask/          # Flask后端（AI服务）
│   ├── routes/           # 路由
│   ├── models/          # 数据模型
│   ├── utils/           # 工具类
│   └── config.py        # 配置文件
│   
├── White Hat Workshop/    # 微信小程序
│   ├── pages/           # 页面
│   ├── components/      # 组件
│   └── utils/           # 工具类
│   
└── VideoApp/             # 安卓App
    ├── app/
    │   ├── src/main/
    │   │   ├── java/    # 源代码
    │   │   └── res/     # 资源文件
    └── gradle/          # 构建配置
```

## 部署指南

### 1. 环境要求
- Node.js 16+
- Java 17+
- Python 3.8+
- MongoDB 4.4+
- Redis 6.0+
- Docker 20.10+

### 2. 前端部署
```bash
# 安装依赖
cd Frontend
npm install

# 开发环境
npm run dev

# 生产构建
npm run build

# 配置nginx
server {
    listen 80;
    server_name your_domain;
    
    location / {
        root /path/to/dist;
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://localhost:8080;
    }
}
```

### 3. SpringBoot后端部署
```bash
# 打包
cd BackendSpringBoot
mvn clean package

# 运行
java -jar target/*.jar --spring.profiles.active=prod

# 或使用Docker
docker build -t bmgf-backend .
docker run -d -p 8080:8080 bmgf-backend
```

### 4. Flask后端部署
```bash
# 创建虚拟环境
cd BackendFlask
python -m venv .venv
source .venv/bin/activate  # Windows: .venv\Scripts\activate

# 安装依赖
pip install -r requirements.txt

# 配置
cp .env.example .env
# 编辑.env文件，配置必要的环境变量

# 运行
python app.py

# 或使用gunicorn
gunicorn -w 4 -b 0.0.0.0:5000 app:app
```

### 5. 数据库配置
```bash
# MongoDB
mongod --auth --bind_ip_all

# Redis
redis-server --requirepass your_password
```

### 6. Docker环境配置
```yaml
# docker-compose.yml
version: '3'
services:
  mongodb:
    image: mongo:4.4
    volumes:
      - ./data/db:/data/db
    
  redis:
    image: redis:6.0
    command: redis-server --requirepass your_password
    
  backend:
    build: ./BackendSpringBoot
    ports:
      - "8080:8080"
    depends_on:
      - mongodb
      - redis
      
  ai-service:
    build: ./BackendFlask
    ports:
      - "5000:5000"
    depends_on:
      - mongodb
      - redis
```

## API文档

### 1. 用户管理
```http
# 注册
POST /api/user/register
Content-Type: application/json

{
    "username": "string",
    "password": "string",
    "email": "string"
}

# 登录
POST /api/user/login
Content-Type: application/json

{
    "username": "string",
    "password": "string"
}
```

### 2. CTF靶场
```http
# 创建靶机
POST /api/lab/create
Authorization: Bearer <token>
Content-Type: application/json

{
    "challenge_id": "string"
}

# 提交flag
POST /api/lab/flag
Authorization: Bearer <token>
Content-Type: application/json

{
    "challenge_id": "string",
    "flag": "string"
}
```

### 3. AI问答
```http
# 发送问题
POST /api/chat
Authorization: Bearer <token>
Content-Type: application/json

{
    "message": "string"
}

# 获取历史记录
GET /api/chat/history
Authorization: Bearer <token>
```

## 安全配置建议

1. 服务器安全
   - 使用防火墙限制端口访问
   - 启用HTTPS
   - 定期更新系统和依赖包
   
2. 应用安全
   - 所有API使用JWT认证
   - 密码加密存储
   - 输入验证和过滤
   - XSS和CSRF防护
   
3. 数据安全
   - 数据库访问控制
   - 敏感信息加密
   - 定期备份

## 常见问题

1. 环境配置问题
   - 确保所有必要服务都已启动
   - 检查端口占用情况
   - 验证配置文件正确性

2. 部署问题
   - 检查日志文件
   - 确认网络连接
   - 验证权限设置

3. 性能优化
   - 使用Redis缓存
   - 数据库索引优化
   - 静态资源CDN

## 开发计划

1. 近期计划
   - 优化AI问答系统
   - 增加更多CTF题目
   - 完善文档系统

2. 长期规划
   - 支持更多漏洞类型
   - 添加实时协作功能
   - 开发iOS版本

## 贡献指南

1. 提交规范
   - 使用清晰的commit信息
   - 创建功能分支
   - 编写测试用例

2. 代码规范
   - 遵循各语言标准规范
   - 添加必要的注释
   - 进行代码审查

## 联系方式

- 邮箱：zhaoqsnyah@gmail.com
- 项目地址：[在线演示](http://wacyg.fun)
- GitHub地址：[GitHub](https://github.com/Songwo/BaiMaoGongFang.git)
- App下载：[Android版本](http://wacyg.fun/apk/白帽工坊.apk)

## 致谢

感谢所有贡献者的付出，让这个项目能够不断完善和进步。

---

*免责声明：本项目仅供学习交流使用，任何未经授权的访问、攻击或数据窃取行为均属违法，必将追究法律责任。* 

## 技术实现详解

### 1. 前端路由与权限控制

```typescript
// router/index.ts - Vue Router配置示例
import { createRouter, createWebHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';
import store from '../store';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/root/home'
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/components/Login_Register.vue')
  },
  {
    path: '/backMange',
    name: 'backMange',
    component: () => import('@/components/backMange/Root/root.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: 'user',
        name: 'user',
        component: () => import('@/components/backMange/UserMange.vue'),
        meta: { requiresAuth: true }
      },
      // ... 其他管理页面路由
    ]
  }
];

// 路由守卫实现
router.beforeEach((to, from, next) => {
  const token = store.state.token || localStorage.getItem('token');
  
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!token || token === 'null' || token === '') {
      ElMessage.warning('该功能需要登录后才能使用');
      next('/');
    } else {
      next();
    }
  } else {
    next();
  }
});
```

### 2. 后端接口实现

#### 用户管理接口
```java
// UserController.java - 用户管理接口示例
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;

    // 用户注册
    @PostMapping("/register")
    public Result register(@Valid @RequestBody RegisterRequest request) {
        // 检查用户名是否已存在
        User user1 = userService.findByUsername(request.getUsername());
        if (user1 != null) {
            return Result.error("该用户名已存在");
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRoles(new HashSet<>(Arrays.asList("ROLE_USER")));
        userService.save(user);
        return Result.success();
    }

    // 用户登录
    @PostMapping("/login")
    public Result login(@Valid @RequestBody LoginRequest request) {
        User user = userService.findByUsername(request.getUsername());
        if (user == null) {
            return Result.error("用户不存在，请注册");
        }
        if (!matches(request.getPassword(), user.getPassword())) {
            return Result.error("密码错误");
        }
        
        // 生成JWT令牌
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtUtil.generateToken(
            user.getUsername(),
            user.getId(),
            userDetails.getAuthorities()
        );
        return Result.success(token);
    }

    // 更新用户信息
    @PutMapping("/update")
    public Result update(@Valid @RequestHeader("Authorization") String authHeader,
                        @Valid @RequestBody User user) {
        String token = authHeader.substring(7);
        String username = jwtUtil.getUsernameFromToken(token);
        String currentUserId = userService.findByUsername(username).getId();
        
        // 权限验证
        if (!currentUserId.equals(user.getId())) {
            return Result.error("无权限操作");
        }
        
        // 更新用户信息
        User existingUser = userService.findById(currentUserId);
        existingUser.setEmail(user.getEmail());
        existingUser.setUsername(user.getUsername());
        userService.save(existingUser);
        return Result.success("用户信息更新成功");
    }
}
```

### 3. 安全配置实现

```java
// SecurityConfig.java - Spring Security配置
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CORS配置
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 禁用CSRF（使用JWT）
            .csrf(AbstractHttpConfigurer::disable)
            
            // 会话管理（无状态）
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 授权配置
            .authorizeHttpRequests(auth -> auth
                // 公开路径
                .requestMatchers(
                    "/user/login",
                    "/user/register",
                    "/error"
                ).permitAll()
                
                // 管理员路径
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // 其他请求需要认证
                .anyRequest().authenticated()
            )
            
            // 添加JWT过滤器
            .addFilterBefore(jwtAuthenticationFilter, 
                UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }
}
```

### 4. 数据库设计

#### MongoDB集合设计
```javascript
// 用户集合
{
  _id: ObjectId,
  username: String,
  password: String(hashed),
  email: String,
  role: String,
  points: Number,
  created_at: Date,
  avatar: String,
  last_login: Date
}

// CTF题目集合
{
  _id: ObjectId,
  title: String,
  description: String,
  difficulty: Number,
  category: String,
  flag: String(encrypted),
  points: Number,
  hints: [String],
  created_at: Date,
  solved_count: Number
}

// 论坛帖子集合
{
  _id: ObjectId,
  title: String,
  content: String,
  author: {
    id: ObjectId,
    username: String
  },
  tags: [String],
  created_at: Date,
  updated_at: Date,
  view_count: Number,
  like_count: Number,
  comment_count: Number
}
```

### 5. Docker部署配置

```yaml
# docker-compose.yml
version: '3'
services:
  # MongoDB服务
  mongodb:
    image: mongo:4.4
    volumes:
      - ./data/db:/data/db
    environment:
      MONGO_INITDB_ROOT_USERNAME: admin
      MONGO_INITDB_ROOT_PASSWORD: your_password
    ports:
      - "27017:27017"
    
  # Redis服务
  redis:
    image: redis:6.0
    command: redis-server --requirepass your_password
    ports:
      - "6379:6379"
    
  # SpringBoot后端
  backend:
    build: ./BackendSpringBoot
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - MONGODB_URI=mongodb://admin:your_password@mongodb:27017
      - REDIS_HOST=redis
      - REDIS_PORT=6379
    depends_on:
      - mongodb
      - redis
      
  # Flask AI服务
  ai-service:
    build: ./BackendFlask
    ports:
      - "5000:5000"
    environment:
      - FLASK_ENV=production
      - MONGODB_URI=mongodb://admin:your_password@mongodb:27017
      - REDIS_HOST=redis
      - REDIS_PORT=6379
    depends_on:
      - mongodb
      - redis
      
  # Nginx前端服务
  nginx:
    image: nginx:latest
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./Frontend/dist:/usr/share/nginx/html
      - ./nginx/conf.d:/etc/nginx/conf.d
    depends_on:
      - backend
      - ai-service
```

### 6. Nginx配置

```nginx
# nginx/conf.d/default.conf
server {
    listen 80;
    server_name your_domain;
    
    # 重定向HTTP到HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl;
    server_name your_domain;
    
    # SSL配置
    ssl_certificate /etc/nginx/ssl/cert.pem;
    ssl_certificate_key /etc/nginx/ssl/key.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    
    # 前端静态文件
    location / {
        root /usr/share/nginx/html;
        try_files $uri $uri/ /index.html;
        expires 30d;
        add_header Cache-Control "public, no-transform";
    }
    
    # API代理
    location /api {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    # AI服务代理
    location /ai {
        proxy_pass http://ai-service:5000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    # 安全配置
    add_header X-Frame-Options "SAMEORIGIN";
    add_header X-XSS-Protection "1; mode=block";
    add_header X-Content-Type-Options "nosniff";
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains";
}
``` 