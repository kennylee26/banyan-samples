# banyan-samples-feign

`banyan-spring-feign` 与 `banyan-trace` 的可运行样例：同一个 Spring Boot 应用既是提供方，也是 Feign 调用方。

## 启动

```bash
SERVER_PORT=18768 mvn spring-boot:run
```

端口默认 `18768`，可用 `SERVER_PORT` 覆盖。

## 配置

`application.yml` 默认：

- header 转发关闭（安全默认）
- 请求/响应 DEBUG 日志开启，敏感值脱敏
- trace 出向透传与响应侧补全开启

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
