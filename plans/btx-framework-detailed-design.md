# BTX Framework v1.3.0-SNAPSHOT 详细设计文档

| 修订记录 | | |
|---------|------|------|
| 版本 | 日期 | 说明 |
| v1.0 | 2026-06-03 | 初始版本，涵盖全部模块的详细设计 |

## 1. 项目概述

BTX Framework 是一个基于 **Spring Boot 3.x + JDK 17** 的企业级应用开发框架，定位为自用 Spring Boot 脚手架（"自用springboot脚手架"）。框架版本为 `1.3.0-SNAPSHOT`，遵循 Apache 2.0 开源协议。

框架的核心设计目标是提供一套开箱即用、模块化、可插拔的企业级开发基础设施，涵盖 **Web 层快速开发、安全认证授权、缓存、数据库访问、分布式会话** 等常见场景。

## 2. 整体架构

### 2.1 项目模块层次结构

```
btx-framework-root                          # 根项目 (pom)
├── btx-framework-parent                    # 全局依赖版本管理父POM
├── btx-framework/
│   ├── btx-framework-dependencies          # BOM依赖管理 (供外部项目导入)
│   ├── btx-framework-core                  # 核心模块
│   ├── btx-framework-boot                  # Spring Boot基础模块
│   ├── btx-framework-boot-web              # Web MVC模块 (Servlet)
│   ├── btx-framework-boot-webflux          # WebFlux响应式模块
│   ├── btx-framework-boot-webflux-redis    # WebFlux Redis缓存模块
│   ├── btx-framework-boot-cache            # 缓存模块 (Redis)
│   ├── btx-framework-boot-security         # 安全认证抽象模块
│   ├── btx-framework-boot-security-shiro   # Shiro安全实现模块
│   ├── btx-framework-boot-security-shiro-redis  # Shiro Redis缓存模块
│   ├── btx-framework-boot-database         # 数据库基础模块 (MyBatis-Plus)
│   ├── btx-framework-boot-database-hikari  # HikariCP数据源加密模块
│   ├── btx-framework-boot-database-reactive    # 响应式数据库模块
│   └── btx-framework-boot-session          # Redis分布式会话模块
├── btx-projects/                           # 业务项目脚手架
└── btx-framework-local/                    # 本地定制化模块
```

### 2.2 模块依赖关系图

```mermaid
graph TB
    Core[btx-framework-core] --> Boot[btx-framework-boot]
    Boot --> Web[btx-framework-boot-web]
    Boot --> Security[btx-framework-boot-security]
    Security --> Shiro[btx-framework-boot-security-shiro]
    Shiro --> ShiroRedis[btx-framework-boot-security-shiro-redis]
    Core --> Cache[btx-framework-boot-cache]
    Cache --> ShiroRedis
    Boot --> Database[btx-framework-boot-database]
    Database --> Hikari[btx-framework-boot-database-hikari]
    Database --> ReactiveDB[btx-framework-boot-database-reactive]
    Boot --> Session[btx-framework-boot-session]
    Boot --> WebFlux[btx-framework-boot-webflux]
    WebFlux --> WebFluxRedis[btx-framework-boot-webflux-redis]
```

### 2.3 核心设计理念

| 理念 | 说明 |
|------|------|
| **约定优于配置** | 框架提供默认配置，大部分场景无需额外配置即可运行 |
| **模块化可插拔** | 各模块通过 `@ConditionalOnProperty` / `@ConditionalOnClass` 控制自动装配 |
| **接口抽象** | 安全认证采用接口抽象 + Shiro 实现的模式，支持多种认证方式 |
| **统一响应模型** | 全局使用 `CommJSON<T>` 统一返回格式 |
| **全局异常处理** | 统一异常处理 + 层次化异常类体系 |

## 3. 核心模块 (btx-framework-core)

### 3.1 模块定位

作为整个框架的基石，提供所有模块共用的基础设施，包括：统一响应模型、异常体系、常量定义。

### 3.2 核心类详解

#### 3.2.1 统一响应模型 - [`CommJSON<T>`](btx-framework/btx-framework-core/src/main/java/top/cheesetree/btx/framework/core/json/CommJSON.java:16)

```mermaid
classDiagram
    class CommJSON~T~ {
        +Integer ret         // 返回码：0=成功，1=业务错误，-1=系统错误
        +String subcode      // 子错误码（业务自定义）
        +String msg          // 返回消息
        +T result           // 泛型数据结果
        +checkSuc() boolean  // 快速判断是否成功
    }
    class ValueObject {
        <<interface>>
    }
    CommJSON ..|> ValueObject
```

**设计要点：**
- `ret` 字段仿照 HTTP 状态码设计：0 表示成功，负数表示系统异常，正数表示业务异常
- 提供多种构造函数，支持链式创建
- `checkSuc()` 方法用于快速判断业务执行是否成功

#### 3.2.2 异常体系

```mermaid
classDiagram
    class RuntimeException {
        <<Java SE>>
    }
    class BtxException {
        +String errcode     // 异常编码
    }
    class BusinessException {
        // 业务异常，ret=1
    }
    class SystemException {
        // 系统异常，ret=-1
    }
    RuntimeException <|-- BtxException
    BtxException <|-- BusinessException
    BtxException <|-- SystemException
```

**异常编码规范**（参见 [`ExceptionCodeUtil`](btx-framework/btx-framework-core/src/main/java/top/cheesetree/btx/framework/core/exception/ExceptionCodeUtil.java:10)）：
格式：`系统编码-业务编码-预留码(0000)-具体异常码(4位数字)`
例如：`SYS-USER-0000-1001`

#### 3.2.3 消息常量 - [`BtxMessage`](btx-framework/btx-framework-core/src/main/java/top/cheesetree/btx/framework/core/constants/BtxMessage.java:13)

| 常量名 | ret | message | 说明 |
|--------|-----|---------|------|
| `SUCCESS` | 0 | "" | 成功 |
| `BUSI_ERROR` | 1 | "业务错误" | 业务逻辑错误 |
| `SYSTEM_ERROR` | -1 | "系统错误" | 系统级错误 |
| `UNKOWN_ERROR` | -1 | "未知错误" | 未知异常 |
| `VALIDATE_ERROR` | 1 | "校验不通过" | 参数校验失败 |

## 4. Spring Boot 基础模块 (btx-framework-boot)

提供 [`ApplicationBeanFactory`](btx-framework/btx-framework-boot/src/main/java/top/cheesetree/btx/framework/boot/spring/ApplicationBeanFactory.java:15) 工具类，实现 `ApplicationContextAware` 接口，在非 Spring 管理的类中也能获取 Spring Bean。

## 5. Web MVC 模块 (btx-framework-boot-web)

### 5.1 功能架构

```mermaid
graph TB
    subgraph "btx-framework-boot-web"
        Config[BtxWebMvcConfiguration] --> FastJson[FastJson2 HTTP Message Converter]
        Config --> BtxWebProperties[配置属性]
        Handler[BtxWebExceptionHandler] --> CommJSON
        Utils[HttpUtil / RequestUtil]
        Model[FileInfoDTO]
    end
    FastJson -->|替换| Jackson[Spring Boot默认Jackson]
    Handler -->|@ControllerAdvice| GlobalException[全局异常拦截]
```

### 5.2 核心类详解

#### 5.2.1 [`BtxWebMvcConfiguration`](btx-framework/btx-framework-boot-web/src/main/java/top/cheesetree/btx/framework/web/config/BtxWebMvcConfiguration.java:30)

**职责：** 替换 Spring MVC 默认的 Jackson 消息转换器为 FastJSON2。

**核心逻辑：**
1. 创建 `FastJsonHttpMessageConverter` 并注册多种 MediaType
2. 配置 FastJSON 特征：`FieldBased`（基于字段序列化）、`SupportArrayToBean`
3. 根据 [`BtxWebProperties`](btx-framework/btx-framework-boot-web/src/main/java/top/cheesetree/btx/framework/web/config/BtxWebProperties.java:13) 配置日期格式（默认 `yyyy-MM-dd HH:mm:ss`）
4. 替换原有的 `AbstractJackson2HttpMessageConverter`

**关键配置项（`btx.web.*`）：**
| 属性 | 默认值 | 说明 |
|------|--------|------|
| `dateformat` | `yyyy-MM-dd HH:mm:ss` | 日期格式化 |
| `writedateusedateformat` | `true` | 是否使用日期格式 |

#### 5.2.2 [`BtxWebExceptionHandler`](btx-framework/btx-framework-boot-web/src/main/java/top/cheesetree/btx/framework/web/handle/BtxWebExceptionHandler.java:29)

**职责：** 全局统一异常处理。

**处理策略：**
- `BtxException` → 根据子类型返回不同 ret 码（BusinessException 返回 1，其他返回 -1）
- `BindException` → 参数校验失败，返回 400+错误详情
- 未知异常 → 生成 UUID 追踪ID，返回 -1

#### 5.2.3 [`HttpUtil`](btx-framework/btx-framework-boot-web/src/main/java/top/cheesetree/btx/framework/web/util/HttpUtil.java:38)

**职责：** 封装 HTTP 客户端操作，基于 Spring 6 的 `RestClient` 和 `RestTemplate`。

**支持功能：**
- GET/POST/PUT/DELETE/PATCH 请求
- JSON/Form 提交
- 文件上传（Multipart）
- HTTPS 双向忽略证书验证
- 自定义超时设置

#### 5.2.4 [`RequestUtil`](btx-framework/btx-framework-boot-web/src/main/java/top/cheesetree/btx/framework/web/util/RequestUtil.java:14)

**职责：** 判断请求是否为 AJAX 异步请求。

**判断依据：**
1. `Accept` 头包含 `application/json`
2. `X-Requested-With` 头包含 `XMLHttpRequest`
3. URI 以 `.json` / `.xml` 结尾
4. 参数 `__ajax=json` 或 `__ajax=xml`

## 6. 安全认证模块

### 6.1 整体设计

```mermaid
graph TB
    subgraph "安全认证架构"
        Interface[抽象接口层] --> Service[用户服务接口 IBtxSecurityUserService]
        Interface --> Permission[权限服务接口 IBtxSecurityPermissionService]
        Interface --> Operation[操作接口 IBtxSecurityOperation]
        
        Operation --> ShiroImpl[BtxSecurityShiroOperation - Shiro实现]
        Operation --> WebFluxImpl[BtxWebfluxOperation - WebFlux实现]
        
        ShiroImpl --> ShiroConfig[BtxShiroConfiguration]
        ShiroConfig --> Filters[Shiro Filter链]
        ShiroConfig --> Realm[Realm认证]
        ShiroConfig --> Cache[Shiro Cache]
    end
```

### 6.2 抽象接口层 (btx-framework-boot-security)

#### 6.2.1 核心接口

| 接口 | 类型参数 | 核心方法 |
|------|---------|---------|
| [`IBtxSecurityOperation<T,A>`](btx-framework/btx-framework-boot-security/src/main/java/top/cheesetree/btx/framework/security/IBtxSecurityOperation.java:13) | T: 用户DTO, A: 认证信息 | `login()`, `logout()`, `getUserId()`, `getUserInfo()`, `getAuthInfo()`, `runas()` |
| [`IBtxSecurityUserService<T>`](btx-framework/btx-framework-boot-security/src/main/java/top/cheesetree/btx/framework/security/IBtxSecurityUserService.java:11) | T: 用户DTO | `login()`, `logout()`, `gethdImgurl()`, `changePwd()`, `getUserInfo()` |
| [`IBtxSecurityPermissionService<T,F,R>`](btx-framework/btx-framework-boot-security/src/main/java/top/cheesetree/btx/framework/security/IBtxSecurityPermissionService.java:14) | T: 菜单, F: 功能, R: 角色 | `getMenu()`, `getFunc()`, `getAllFunc()`, `getRole()` |

#### 6.2.2 数据模型

| 类 | 说明 |
|----|------|
| [`SecurityUserDTO`](btx-framework/btx-framework-boot-security/src/main/java/top/cheesetree/btx/framework/security/model/SecurityUserDTO.java) | 用户基本信息（uid, name, funcs, roles, group等） |
| [`SecurityAuthUserDTO`](btx-framework/btx-framework-boot-security/src/main/java/top/cheesetree/btx/framework/security/model/SecurityAuthUserDTO.java) | 认证用户（包含用户信息+认证信息） |
| [`SecurityRoleDTO`](btx-framework/btx-framework-boot-security/src/main/java/top/cheesetree/btx/framework/security/model/SecurityRoleDTO.java) | 角色（roleCode, roleName） |
| [`SecurityFuncDTO`](btx-framework/btx-framework-boot-security/src/main/java/top/cheesetree/btx/framework/security/model/SecurityFuncDTO.java) | 功能权限（funcCode, funcName, actionLink） |
| [`SecurityMenuDTO`](btx-framework/btx-framework-boot-security/src/main/java/top/cheesetree/btx/framework/security/model/SecurityMenuDTO.java) | 菜单（menuCode, menuName, parentCode, url, icon等） |
| [`SecurityGroupDTO`](btx-framework/btx-framework-boot-security/src/main/java/top/cheesetree/btx/framework/security/model/SecurityGroupDTO.java) | 用户组（groupCode, name, roles, funcs等） |

#### 6.2.3 认证类型枚举

```java
public enum AuthType {
    SESSION,    // 基于 Session 的认证（表单登录）
    TOKEN,      // 基于 Token 的认证（用户名+密码）
    JWT,        // JWT 认证（预留）
    CAS,        // CAS 单点登录
    EXT_TOKEN   // 外部 Token 认证（仅需 Token 串）
}
```

### 6.3 Shiro 安全实现模块 (btx-framework-boot-security-shiro)

#### 6.3.1 架构设计

```mermaid
flowchart LR
    req[HTTP Request] --> CORS[CORS Filter]
    req --> CSRF[CSRF Filter]
    req --> ShiroFilter[ShiroFilterFactoryBean]
    
    ShiroFilter --> Token[BtxSecurityShiroTokenFilter]
    ShiroFilter --> Form[BtxSecurityShiroFormFilter]
    ShiroFilter --> CAS[BtxSecurityShiroCasFilter]
    ShiroFilter --> Perms[BtxSecurityShiroPermissionsFilter]
    
    Token --> Realm[BtxSecurityAuthorizingRealm]
    Form --> Realm
    CAS --> CasRealm[BtxSecurityCasAuthorizingRealm]
    
    Realm --> Auth{doGetAuthenticationInfo}
    Realm --> Authz{doGetAuthorizationInfo}
```

#### 6.3.2 [`BtxShiroConfiguration`](btx-framework/btx-framework-boot-security-shiro/src/main/java/top/cheesetree/btx/framework/security/shiro/config/BtxShiroConfiguration.java:58)

**职责：** Shiro 核心配置类，装配完整的 Shiro 安全环境。

**主要 Bean 装配：**

| Bean | 说明 |
|------|------|
| `shiroFilter` | Shiro 过滤器工厂，配置认证过滤器、权限映射 |
| `authenticator` | `BtxModularRealmAuthenticator`，支持多 Realm 策略 |
| `shiroCacheManager` | `MemoryConstrainedCacheManager`（默认内部缓存） |
| `subjectFactory` | `StatelessDefaultSubjectFactory`，禁用 Session 创建 |
| `sessionManager` | `DefaultWebSessionManager`，配置 Session 超时 |
| `securityManager` | `DefaultWebSecurityManager`，组合上述组件 |

**过滤器链配置逻辑：**
1. 排除路径（`contextInterceptorExcludePathPatterns`）→ `anon`
2. 登录页、未授权页、错误页、过期页 → `anon`
3. 自动权限映射（`autoPermission=true`）→ 从数据库加载所有功能点，自动匹配 `perms[funcCode]`
4. 根据 `authType` 选择认证过滤器（Token/Form/CAS）
5. 所有剩余路径 → `authc`

#### 6.3.3 认证过滤器体系

| 过滤器 | 适用认证类型 | 说明 |
|--------|-------------|------|
| [`BtxSecurityShiroTokenFilter`](btx-framework/btx-framework-boot-security-shiro/src/main/java/top/cheesetree/btx/framework/security/shiro/filter/BtxSecurityShiroTokenFilter.java:31) | TOKEN, EXT_TOKEN | 从 Header/Parameter 读取 Token，创建 `StatelessToken` 提交认证 |
| [`BtxSecurityShiroFormFilter`](btx-framework/btx-framework-boot-security-shiro/src/main/java/top/cheesetree/btx/framework/security/shiro/filter/BtxSecurityShiroFormFilter.java:24) | SESSION | 继承 Shiro 的 `FormAuthenticationFilter`，支持 AJAX 响应 |
| [`BtxSecurityShiroCasFilter`](btx-framework/btx-framework-boot-security-shiro/src/main/java/top/cheesetree/btx/framework/security/shiro/support/cas/BtxSecurityShiroCasFilter.java) | CAS | CAS 单点登录票据验证 |
| [`BtxSecurityShiroUserFilter`](btx-framework/btx-framework-boot-security-shiro/src/main/java/top/cheesetree/btx/framework/security/shiro/filter/BtxSecurityShiroUserFilter.java:24) | 通用 | 用户身份验证过滤器，支持 AJAX 响应 |
| [`BtxSecurityShiroPermissionsFilter`](btx-framework/btx-framework-boot-security-shiro/src/main/java/top/cheesetree/btx/framework/security/shiro/filter/BtxSecurityShiroPermissionsFilter.java:25) | 通用 | 权限授权过滤器，支持 AJAX 响应 |

#### 6.3.4 Token 认证流程

```mermaid
sequenceDiagram
    participant Client as 客户端
    participant TokenFilter as BtxSecurityShiroTokenFilter
    participant Realm as BtxSecurityAuthorizingRealm
    participant UserService as IBtxSecurityUserService
    participant Authz as 授权检查

    Client->>TokenFilter: HTTP Request + Authorization Header
    TokenFilter->>TokenFilter: 提取Token
    TokenFilter->>Realm: doGetAuthenticationInfo(token)
    Realm->>UserService: login(username, password/token)
    UserService-->>Realm: CommJSON<SecurityUserDTO>
    Realm->>Realm: 创建 BtxShiroSecurityAuthUserDTO
    Realm-->>TokenFilter: SimpleAuthenticationInfo
    TokenFilter-->>Client: 认证通过
    
    Note over Client,Authz: 后续请求
    Client->>Realm: 带Token的请求
    Realm->>Authz: doGetAuthorizationInfo
    Authz->>Authz: 从用户/数据库获取角色和权限
    Authz-->>Realm: SimpleAuthorizationInfo
```

#### 6.3.5 Realm 设计

[`BtxSecurityAuthorizingRealm`](btx-framework/btx-framework-boot-security-shiro/src/main/java/top/cheesetree/btx/framework/security/shiro/realm/BtxSecurityAuthorizingRealm.java:35)

- **认证（doGetAuthenticationInfo）**：调用 `IBtxSecurityUserService.login()` 进行用户认证，成功后包装为 `BtxShiroSecurityAuthUserDTO`
- **授权（doGetAuthorizationInfo）**：优先从已缓存的用户对象中获取角色/权限，否则调用 `IBtxSecurityPermissionService`
- **缓存支持**：通过 `BtxShiroCacheProperties` 控制认证/授权缓存

#### 6.3.6 安全过滤器链（CORS/CSRF）

| 过滤器 | 条件 | 说明 |
|--------|------|------|
| [`BtxSecurityShiroCorsFilter`](btx-framework/btx-framework-boot-security-shiro/src/main/java/top/cheesetree/btx/framework/security/shiro/filter/BtxSecurityShiroCorsFilter.java:24) | `btx.security.shiro.cors.enabled=true` | CORS 跨域支持，配置允许的 Origin/Methods/Headers |
| [`BtxSecurityShiroCsrfFilter`](btx-framework/btx-framework-boot-security-shiro/src/main/java/top/cheesetree/btx/framework/security/shiro/filter/BtxSecurityShiroCsrfFilter.java:29) | `btx.security.shiro.csrf.enabled=true` | CSRF 防护，通过 Referer 白名单校验 |

#### 6.3.7 异常处理

[`BtxShiroExceptionHandler`](btx-framework/btx-framework-boot-security-shiro/src/main/java/top/cheesetree/btx/framework/security/shiro/config/BtxShiroExceptionHandler.java:28)

- `UnauthorizedException` / `AuthorizationException` → 403 Forbidden
- `UnauthenticatedException` / `AuthenticationException` → 401 Unauthorized

### 6.4 Shiro Redis 缓存模块 (btx-framework-boot-security-shiro-redis)

当 `btx.security.shiro.cache.cache-type=REDIS` 时，使用 Redis 作为 Shiro 缓存。

- [`RedisShiroCacheManager`](btx-framework/btx-framework-boot-security-shiro-redis/src/main/java/top/cheesetree/btx/framework/security/shiro/cache/redis/RedisShiroCacheManager.java:18)：实现 `CacheManager` 接口，创建 `RedisShiroCache`
- [`RedisShiroCache`](btx-framework/btx-framework-boot-security-shiro-redis/src/main/java/top/cheesetree/btx/framework/security/shiro/cache/redis/RedisShiroCache.java:21)：实现 `Cache<K,V>` 接口，使用 RedisTemplate 操作缓存，支持 Lua 脚本实现原子性 get-del 操作

### 6.5 Shiro Session 无状态化

[`StatelessDefaultSubjectFactory`](btx-framework/btx-framework-boot-security-shiro/src/main/java/top/cheesetree/btx/framework/security/shiro/subject/StatelessDefaultSubjectFactory.java:12) 在 `TOKEN` / `EXT_TOKEN` / `JWT` 模式下禁用 Session 创建，实现无状态认证。

## 7. 缓存模块 (btx-framework-boot-cache)

### 7.1 架构设计

```mermaid
graph TB
    subgraph "缓存模块"
        Configure[BtxRedisCacheConfigure] --> Manager[BtxRedisCacheManager]
        Configure --> Factory[RedisTemplateFactoryImpl]
        Manager --> Cache[BtxRedisCache]
        Factory --> Serializers[序列化器体系]
        Serializers --> FastJson[BtxFastJsonRedisSerializer]
        Serializers --> KeyString[BtxKeyStringRedisSerializer]
        Serializers --> BtxRedisSerializer[BtxRedisSerializer组合类]
    end
```

### 7.2 核心类详解

#### 7.2.1 [`BtxRedisCacheConfigure`](btx-framework/btx-framework-boot-cache/src/main/java/top/cheesetree/btx/framework/cache/redis/BtxRedisCacheConfigure.java:35)

**条件：** `spring.cache.type=redis` 时自动配置

**功能：**
1. 创建 `BtxRedisCacheManager`，支持全局和按缓存名称的差异化 TTL 配置
2. 使用 FastJSON2 的 `GenericFastJsonRedisSerializer` 进行值序列化
3. 注册 `RedisTemplateFactoryImpl` Bean 用于创建自定义 RedisTemplate

#### 7.2.2 [`BtxRedisCacheManager`](btx-framework/btx-framework-boot-cache/src/main/java/top/cheesetree/btx/framework/cache/redis/BtxRedisCacheManager.java:22)

继承 Spring Data Redis 的 `RedisCacheManager`，重写 `createRedisCache` 方法以支持：
- 按缓存名称独立配置 TTL（通过 `btx.redis.cache.caches.<cache-name>.*`）
- 动态创建缓存配置

#### 7.2.3 [`RedisTemplateFactoryImpl`](btx-framework/btx-framework-boot-cache/src/main/java/top/cheesetree/btx/framework/cache/redis/RedisTemplateFactoryImpl.java:23)

工厂类，支持按 Key/Value 类型生成对应的 `RedisTemplate`：
- Key 为 String 时使用 `BtxKeyStringRedisSerializer`（支持前缀）
- Value 为非 String 时使用 `BtxFastJsonRedisSerializer`
- 内部使用 `ConcurrentHashMap` 缓存已创建的 RedisTemplate 实例

#### 7.2.4 可配置属性

```yaml
btx:
  redis:
    cache:
      use-key-prefix: true
      key-prefix: "btx:"
      cache-null-values: true
      time-to-live: 3600s
      caches:
        myCache:
          time-to-live: 1800s
          use-key-prefix: true
          key-prefix: "my:"
```

## 8. 数据库模块

### 8.1 基础数据库模块 (btx-framework-boot-database)

基于 MyBatis-Plus 的自动配置：

- [`MybatisPlusConfigure`](btx-framework/btx-framework-boot-database/src/main/java/top/cheesetree/btx/framework/database/config/MybatisPlusConfigure.java:17)：配置分页插件，Mapper 扫描路径可配置
- [`Page`](btx-framework/btx-framework-boot-database/src/main/java/top/cheesetree/btx/framework/database/page/Page.java)：基础分页参数（pageSize, pageIndex）
- [`PageQuery`](btx-framework/btx-framework-boot-database/src/main/java/top/cheesetree/btx/framework/database/page/PageQuery.java)：带排序的分页查询参数
- [`PageResult`](btx-framework/btx-framework-boot-database/src/main/java/top/cheesetree/btx/framework/database/page/PageResult.java)：分页结果，包含 total、data、pageCount 计算

### 8.2 HikariCP 数据源加密模块 (btx-framework-boot-database-hikari)

- [`BtxHikariConfigure`](btx-framework/btx-framework-boot-database-hikari/src/main/java/top/cheesetree/btx/framework/database/hikari/BtxHikariConfigure.java:27)：提供 AES 加解密和 MD5 工具方法
- [`BtxHikariBeanPostProcessor`](btx-framework/btx-framework-boot-database-hikari/src/main/java/top/cheesetree/btx/framework/database/hikari/BtxHikariBeanPostProcessor.java:20)：作为 `BeanPostProcessor`，在 HikariCP 数据源初始化后解密密码

**密码加密机制：**
1. 使用 `appId + "_" + secretKey + "_" + appId` 的 MD5 作为 AES 密钥
2. 密钥的第 3-19 位作为 AES CBC 模式的 IV
3. 密文使用 Base64 URL 编码

```yaml
btx:
  datasource:
    app-id: your-app-id
    secret-key: your-secret-key
```

## 9. WebFlux 响应式模块 (btx-framework-boot-webflux)

### 9.1 架构设计

```mermaid
graph TB
    subgraph "WebFlux模块"
        Config[BtxWebfluxConfiguration] --> CorsFilter[CORS WebFilter]
        Config --> ExchangeFilter[Exchange Context WebFilter]
        Config --> FastJsonCodec[FastJSON2 CodecCustomizer]
        
        Security[BtxWebfluxSecurityConfiguration] --> TokenFilter[TokenFilter]
        Security --> ProviderManager[ProviderManager]
        Security --> SecurityContext[SecurityContextHolder]
        
        TokenFilter --> AuthManager[AuthenticationManager]
        AuthManager --> Provider[BtxUserAuthenticationProvider]
        Provider --> UserService[IBtxSecurityUserService]
    end
```

### 9.2 核心功能

- CORS 跨域支持（通过 WebFilter 实现）
- Request Context 持有（通过 Reactor Context 传递 `ServerWebExchange`）
- FastJSON2 编解码（替换默认 Jackson）
- 无状态安全认证（Token 模式）

### 9.3 WebFlux 安全认证

[`BtxWebfluxOperation`](btx-framework/btx-framework-boot-webflux/src/main/java/top/cheesetree/btx/framework/webflux/security/BtxWebfluxOperation.java:38)

- 使用 `AuthenticationManager` 管理认证流程
- 支持 Token 缓存（Caffeine / Redis）
- `SecurityContextHolder` 基于 Reactor Context 实现线程安全

## 10. 分布式会话模块 (btx-framework-boot-session)

基于 Spring Session Data Redis 实现分布式会话：

- [`BtxRedisSessionConfig`](btx-framework/btx-framework-boot-session/src/main/java/top/cheesetree/btx/framework/web/session/config/BtxRedisSessionConfig.java:12)：配置 Redis HTTP Session 存储
- [`BtxSessionConfigProperties`](btx-framework/btx-framework-boot-session/src/main/java/top/cheesetree/btx/framework/web/session/config/BtxSessionConfigProperties.java:17)：Session 配置（namespace、timeout、saveMode）

## 11. 配置属性汇总

### 11.1 Web 配置 (`btx.web.*`)

```yaml
btx:
  web:
    dateformat: yyyy-MM-dd HH:mm:ss
    writedateusedateformat: true
```

### 11.2 安全配置 (`btx.security.*`)

```yaml
btx:
  security:
    context-interceptor-exclude-path-patterns:
      - /api/public/**
    error-path: /error
    login-path: /login
    no-auth-path: /unauth
    expire-path: /expire
    shiro:
      session-time-out: 1800
      auth-type: TOKEN       # SESSION | TOKEN | JWT | CAS | EXT_TOKEN
      token-key: Authorization
      auto-permission: false
      ignore-token: false
      cookie-name:
      cache:
        enabled: true
        cache-type: INNER     # INNER | REDIS
        authentication-cache-name: authenticationCache
        authorization-cache-name: authorizationCache
        cache-expire: 1800
      cors:
        enabled: false
        allow-credentials: "true"
      csrf:
        enabled: false
      cas:
        server-url-prefix:
        server-login-url:
        client-host-url:
        validation-type: CAS3
        proxy-callback-url:
        proxy-receptor-url:
        skip-ticket-validation: false
        dev-user-name:
```

### 11.3 Redis 缓存配置 (`btx.redis.cache.*`)

```yaml
btx:
  redis:
    cache:
      use-key-prefix: true
      key-prefix: "btx:"
      cache-null-values: true
      time-to-live: 3600s
      caches:
        cacheName:
          time-to-live: 1800s
```

### 11.4 数据库配置

```yaml
btx:
  datasource:
    app-id: 
    secret-key:
mybatis-plus:
  mapper-scan: top.cheesetree.**.mapper
```

### 11.5 Session 配置

```yaml
btx:
  session:
    namespace: spring:session
btx.session.timeout: PT30M
```

## 12. 模块使用建议

| 场景 | 推荐依赖 |
|------|---------|
| 基础 REST API 开发 | `btx-framework-boot-web` |
| 需要用户认证授权 | + `btx-framework-boot-security-shiro` |
| 需要 Redis 缓存 | + `btx-framework-boot-cache` |
| Shiro 缓存用 Redis | + `btx-framework-boot-security-shiro-redis` |
| 需要 MyBatis-Plus | + `btx-framework-boot-database` |
| HikariCP 密码加密 | + `btx-framework-boot-database-hikari` |
| 分布式 Session | + `btx-framework-boot-session` |
| 响应式 WebFlux | + `btx-framework-boot-webflux` |
| WebFlux Redis 缓存 | + `btx-framework-boot-webflux-redis` |
