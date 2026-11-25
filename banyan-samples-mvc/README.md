# banyan-samples-mvc

`banyan-spring-mvc` 的可运行样例：验证异常 advice、可替换错误响应与可选 Jackson 增强在真实 Spring Boot 应用里的行为。

## 验证什么

- 正常响应**不**被包装（模块不注册成功响应的 `ResponseBodyAdvice`）。
- 请求参数错误（缺失、类型不匹配）被转换为中性错误响应，HTTP 200。
- 未预期异常由兜底 advice 承接，响应不回显原始异常信息。
- 使用方实现 `ExceptionResultFactory` 时，默认结构让位，响应体形状由使用方决定。
- `banyan.spring.mvc.enabled=false` 时本模块 bean 全部不注册。
- `banyan.spring.mvc.jackson.enabled=true` 时 `Long` 序列化为字符串。

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

`application.yml` 默认：

```yaml
banyan:
  spring:
    mvc:
      enabled: true
```

其余可用键（默认值见 `banyan-spring-mvc` 模块的 `banyan-spring-mvc.yml`）：

| 键 | 默认 | 说明 |
|---|---|---|
| `banyan.spring.mvc.enabled` | `true` | 总开关 |
| `banyan.spring.mvc.web-exception.enabled` | `true` | MVC 参数/绑定/校验异常 advice |
| `banyan.spring.mvc.feign-exception.enabled` | `true` | Feign 异常 advice（classpath 无 Feign 时跳过） |
| `banyan.spring.mvc.fallback-exception.enabled` | `true` | 兜底异常 advice |
| `banyan.spring.mvc.jackson.enabled` | `false` | 可选 Jackson 增强总开关 |
| `banyan.spring.mvc.jackson.long-as-string` | `true` | Long 转字符串（仅增强开启时生效） |
| `banyan.spring.mvc.jackson.enums-using-name` | `true` | 枚举按 name（仅增强开启时生效） |
| `banyan.spring.mvc.jackson.big-decimal-as-plain` | `true` | BigDecimal 非科学计数法（仅增强开启时生效） |

日期格式、时区、Jackson feature 开关直接用 Spring Boot 原生 `spring.jackson.*`，本模块不重复定义。

## 测试用例索引

| 测试类 | 场景 |
|---|---|
| `MvcSampleEndToEndTest` | 正常响应不包装、缺少参数、参数类型不匹配、兜底异常不回显原始信息、默认 bean 装配 |
| `CustomFactorySampleTest` | `custom-factory` profile 下自定义错误响应结构 |
| `DisabledSampleTest` | 总开关关闭后本模块 bean 全部不注册 |
| `JacksonEnhancementSampleTest` | 开启 Jackson 增强后 Long 输出字符串 |

所有用例使用 `MockMvc`，不依赖外部服务。

## 测试

```bash
mvn -pl banyan-samples-mvc test
```

先确保 banyan 库已安装到本地仓库：

```bash
(cd ../../banyan-starter && mvn clean install -DskipTests)
```
