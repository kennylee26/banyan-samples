# banyan-samples-feign

`banyan-spring-feign` 与 `banyan-trace` 的可运行样例：同一个 Spring Boot 应用既是提供方，也是 Feign 调用方。

## 启动

```bash
SERVER_PORT=18768 mvn spring-boot:run
```

端口默认 `18768`，可用 `SERVER_PORT` 覆盖。

## 关键配置项

`application.yml` 显式列出 `banyan-spring-feign` 与 `banyan-trace` 的相关键（与 [`application.yml`](src/main/resources/application.yml) 一致）：

### banyan-spring-feign

| 配置项 | 样例值 | 默认值 / 说明 |
|---|---|---|
| `banyan.spring.feign.enabled` | `true` | 总开关，默认 `true` |
| `banyan.spring.feign.forward-headers.enabled` | `false` | 入站 header 转发，安全默认关闭 |
| `banyan.spring.feign.forward-headers.clear-after-apply` | `true` | apply 后自动清理上下文，默认 `true` |
| `banyan.spring.feign.forward-headers.blocked-headers` | `[]` | 额外阻塞头，默认空 |
| `banyan.spring.feign.request-log.enabled` | `true` | DEBUG 请求日志，默认 `true` |
| `banyan.spring.feign.response-log.enabled` | `true` | DEBUG 响应日志，默认 `true` |
| `banyan.spring.feign.logging.mask-sensitive` | `true` | 敏感 header 脱敏，默认 `true` |
| `banyan.spring.feign.logging.masked-headers` | `Authorization,Cookie,Set-Cookie` | 脱敏名单 |
| `banyan.spring.feign.logging.ignored-headers` | `[]` | 完全不打印的 header，默认空 |
| `banyan.spring.feign.logging.max-body-length` | `2048` | body 打印上限，默认 `2048` |

### banyan-trace

| 配置项 | 样例值 | 默认值 / 说明 |
|---|---|---|
| `banyan.trace.enabled` | `true` | 总开关，默认 `true` |
| `banyan.trace.header-name` | `X-Trace-Id` | 传输 header 名 |
| `banyan.trace.mdc-key` | `traceId` | 日志 MDC key |
| `banyan.trace.accept-incoming` | `true` | 沿用上游 TraceId，默认 `true` |
| `banyan.trace.servlet.enabled` | `true` | Servlet 过滤器，默认 `true` |
| `banyan.trace.async.decorate-executors` | `true` | 自动装饰容器线程池，默认 `true` |
| `banyan.trace.feign.enabled` | `true` | Feign 出向透传，默认 `true` |
| `banyan.trace.feign.response-enabled` | `true` | 响应侧 TraceId 补全，默认 `true` |

完整默认值与注释见两个库模块各自的 `<artifactId>.yml`。

## 测试用例索引

| 测试类 | 场景 |
|---|---|
| `FeignSampleEndToEndTest` | 默认不转发 header、响应日志真实生效且不破坏 Decoder、traceId 出向透传与响应侧补全、真实链路转发 header 并阻塞敏感头、单次禁用转发、脱敏关闭、全局开关 |

所有用例在 `banyan-samples-feign` 模块内完成，不依赖外部服务。

## 测试

```bash
mvn -pl banyan-samples-feign test
```

先确保 banyan 库已安装到本地仓库：

```bash
(cd ../../banyan-starter && mvn clean install -DskipTests)
```
