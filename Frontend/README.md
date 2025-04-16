# BaiMaoGongFang 前端项目

本项目基于 **Vue 3** + **Vite**，集成了 Element Plus、Vuetify、Vuex、Vue Router、Echarts 等现代前端技术栈，适用于多功能社区、游戏、AI 问答等场景。

## 目录结构

```
Frontend/
├── public/                # 公共静态资源（图片、模型、视频等）
├── src/
│   ├── api/               # API 接口封装
│   ├── assets/            # 静态资源（图片、样式等）
│   ├── axios/             # axios 实例与拦截器
│   ├── components/        # 业务组件
│   │   ├── home/          # 首页及社区相关组件
│   │   ├── backMange/     # 后台管理相关组件
│   │   ├── gameView/      # 游戏相关组件
│   │   ├── app_download/  # APP 下载页组件
│   │   |── ai_answer/     # AI 问答相关组件
│   ├── plugins/           # 插件（如 Vuetify 配置）
│   ├── router/            # 路由配置
│   ├── store/             # Vuex 状态管理
│   ├── styles/            # 全局样式
│   ├── types/             # 类型定义
│   ├── utils/             # 工具函数
│   ├── App.vue            # 根组件
│   └── main.ts            # 入口文件
├── index.html             # 入口 HTML
├── package.json           # 项目依赖与脚本
├── tsconfig*.json         # TypeScript 配置
├── vite.config.ts         # Vite 配置
└── README.md              # 项目说明文档
```

## 主要依赖

- **Vue 3**：主流渐进式前端框架
- **Vite**：极速开发与构建工具
- **Element Plus**：UI 组件库
- **Vuetify**：Material Design 风格 UI 组件库
- **Vuex**：状态管理
- **Vue Router**：前端路由
- **Echarts**：数据可视化
- **axios**：HTTP 请求库
- **lodash-es**、**highlight.js**、**marked** 等工具库

## 快速开始

### 安装依赖

```bash
npm install
```

### 本地开发

```bash
npm run dev
```

### 生产环境构建

```bash
npm run build
```

### 预览生产环境

```bash
npm run preview
```

### 类型检查

```bash
npm run type-check
```

## 路由与核心页面

- `/` 登录/注册页
- `/root` 主页（含社区、隐私工具、游戏、AI 问答、关于我们等子页面）
- `/backMange` 后台管理（含评论、数据、用户、视频、BUG、分类、问题、奖励等子模块）

所有需要登录的页面均有路由守卫，未登录会自动跳转到登录页。

## 主要功能模块

- **社区交流**：支持社区发帖、评论、提问、奖励等互动
- **隐私工具**：如 CMS 探测、Base64 编解码、IP 查询、Hash 生成、密码校验等
- **游戏体验**：docker动态Ctf靶场
- **AI 问答**：集成 AI 聊天与问答
- **后台管理**：多维度内容与用户管理
- **多端适配**：内置移动端适配样式

## 技术亮点

- 支持 Element Plus 与 Vuetify 双 UI 框架
- 丰富的业务组件，易于扩展
- 现代化 TypeScript 支持
- 代码自动导入与组件自动注册
- 高度模块化，便于维护和协作

## 贡献与开发建议

- 推荐使用 [VSCode](https://code.visualstudio.com/) + [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) 进行开发
- 代码风格遵循 TypeScript 规范
- 组件开发建议放在 `src/components` 下按业务模块分类
- 公共工具函数放在 `src/utils`，类型定义放在 `src/types`

## 参考文档

- [Vue 3 官方文档](https://v3.vuejs.org/)
- [Vite 官方文档](https://vitejs.dev/)
- [Element Plus](https://element-plus.org/)
- [Vuetify](https://vuetifyjs.com/)

---