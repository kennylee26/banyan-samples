# banyan-samples-mvc

`banyan-spring-mvc` 的可运行样例：验证异常 advice、可替换错误响应与默认开启的 Jackson 增强在真实 Spring Boot 应用里的行为。

## 验证什么

- 正常响应**不**被包装（模块不注册成功响应的 `ResponseBodyAdvice`）。
- 请求参数错误（缺失、类型不匹配）被转换为中性错误响应，HTTP 200。
- 未预期异常由兜底 advice 承接，响应不回显原始异常信息。
- 使用方实现 `ExceptionResultFactory` 时，默认结构让位，响应体形状由使用方决定。
- `banyan.spring.mvc.enabled=false` 时本模块 bean 全部不注册。
- Jackson 增强**默认开启**：`Long` 转字符串；`Date` 与 `LocalDateTime` 按 `date-format`、`LocalDate` 按 `local-date-format` 输出；`jackson.enabled=false` 可关闭。

## 启动

```bash
mvn spring-boot:run
```

默认 `server.port=0`（随机端口）；需要固定端口时用 `SERVER_PORT` 覆盖：

```bash
SERVER_PORT=18769 mvn spring-boot:run
```

自定义错误响应样例使用 `custom-factory` profile：

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=custom-factory
```

## 关键配置项

`application.yml` 显式列出的关键项（完整默认值与注释见 `banyan-spring-mvc` 模块的 `banyan-spring-mvc.yml`）：

```yaml
banyan:
  spring:
    mvc:
      enabled: true
      jackson:
        enabled: true
        date-format: yyyy-MM-dd HH:mm:ss
```

| 键 | 默认 | 说明 |
|---|---|---|
| `banyan.spring.mvc.enabled` | `true` | 总开关 |
| `banyan.spring.mvc.web-exception.enabled` | `true` | MVC 参数/绑定/校验异常 advice |
| `banyan.spring.mvc.feign-exception.enabled` | `true` | Feign 异常 advice（classpath 无 Feign 时跳过） |
| `banyan.spring.mvc.fallback-exception.enabled` | `true` | 兜底异常 advice |
| `banyan.spring.mvc.jackson.enabled` | `true` | Jackson 增强总开关；`false` 恢复应用原有 JSON 输出 |
| `banyan.spring.mvc.jackson.long-as-string` | `true` | Long 转字符串 |
| `banyan.spring.mvc.jackson.enums-using-name` | `true` | 枚举按 name |
| `banyan.spring.mvc.jackson.big-decimal-as-plain` | `true` | BigDecimal 非科学计数法 |
| `banyan.spring.mvc.jackson.lenient-boolean` | `true` | 布尔接受 true/false、0/1、字符串 "true"/"false"/"0"/"1" |
| `banyan.spring.mvc.jackson.date-format` | `yyyy-MM-dd HH:mm:ss` | Date 与 LocalDateTime 的输入/输出格式；空表示不处理日期 |
| `banyan.spring.mvc.jackson.local-date-format` | `yyyy-MM-dd` | LocalDate 的输入/输出格式；不能带时间字段 |
| `banyan.spring.mvc.jackson.time-zone` | （空） | 日期时区；空表示沿用应用默认 |

其余 Jackson feature（包含策略、`SerializationFeature` 等）仍用 Spring Boot 原生 `spring.jackson.*`。

## 测试用例索引

| 测试类 | 场景 |
|---|---|
| `MvcSampleEndToEndTest` | 正常响应不包装、Long 转字符串、LocalDateTime / LocalDate 格式化、缺少参数、参数类型不匹配、兜底异常不回显原始信息、默认 bean 装配 |
| `CustomFactorySampleTest` | `custom-factory` profile 下自定义错误响应结构 |
| `DisabledSampleTest` | 总开关关闭后本模块 bean 全部不注册 |
| `JacksonEnhancementSampleTest` | 默认开启下 Long 输出字符串、Date / LocalDateTime / LocalDate 按统一格式输出 |

所有用例使用 `MockMvc`，不依赖外部服务。

## 测试

```bash
mvn -pl banyan-samples-mvc test
```

先确保 banyan 库已安装到本地仓库：

```bash
(cd ../../banyan-starter && mvn clean install -DskipTests)
```
