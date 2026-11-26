# banyan-samples

[English](README.md) | [简体中文](README.zh-CN.md)

[banyan](https://github.com/kennylee26/banyan) 的可运行示例与集成测试。banyan 是一组可复用的 Spring Boot 基础设施库；本仓是 banyan 的使用示例，不发布、本身也不是库。

## 模块

| 模块 | 说明 |
|---|---|
| [`banyan-samples-mq/`](banyan-samples-mq/README.md) | `banyan-mq` 示例：Kafka / RocketMQ 的生产者与消费者、多后端路由、payload 类型 |
| [`banyan-samples-sign/`](banyan-samples-sign/README.md) | `banyan-sign` 示例：内嵌签名提供方 + HttpClient5 / RestTemplate 客户端 |
| [`banyan-samples-feign/`](banyan-samples-feign/README.md) | `banyan-spring-feign` 示例：header 转发、日志与 traceId 透传 |
| [`banyan-samples-mvc/`](banyan-samples-mvc/README.md) | `banyan-spring-mvc` 示例：分级异常 advice、可替换错误响应与可选 Jackson 增强 |
| [`banyan-samples-excel/`](banyan-samples-excel/README.md) | `banyan-excel` 示例：分页批次导出、模板、自动分 sheet 与 CSV |

## 约定

- 每个模块是可独立启动的 Spring Boot 应用：示例代码在 `src/main/java`，测试在 `src/test/java`。
- 多数模块 `server.port` 默认 `0`（随机）；需要稳定客户端地址的模块使用固定偏门端口（18761–18768），可用 `SERVER_PORT` 覆盖。
- 连接地址通过 `application.yml` 的 `${环境变量:默认值}` 占位符配置，不在代码或测试里写死。
- 测试优先使用内嵌基础设施：Kafka 用 `EmbeddedKafka`，RocketMQ 用 Testcontainers，不依赖外部 broker。

## 构建

示例依赖 `cn.kennylee.banyan:banyan-mq-*` 与 `banyan-sign-*` / `banyan-spring-*` / `banyan-excel-*:0.1.0-SNAPSHOT`，先到 [banyan](https://github.com/kennylee26/banyan) 仓安装库：

```bash
# sign 示例（在 banyan 仓根目录执行全量 install）
mvn clean install -DskipTests

# mq 示例
mvn -pl banyan-mq/banyan-mq-core,banyan-mq/banyan-mq-commons-spring,banyan-mq/banyan-mq-producer,banyan-mq/banyan-mq-consumer,banyan-mq/banyan-mq-kafka-spring,banyan-mq/banyan-mq-rocketmq-apache install -DskipTests
```

然后在本仓执行：

```bash
mvn clean test
```

各模块怎么单独启动、测试用例索引，见对应模块的 README。
