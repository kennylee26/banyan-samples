# banyan-samples

banyan 的使用示例与集成测试，独立于库本身，不发布。

## 模块

| 模块 | 说明 |
|---|---|
| [`banyan-samples-mq/`](banyan-samples-mq/README.md) | `banyan-mq` 示例：生产者 / 消费者 × Kafka / RocketMQ |

## 约定

- 每个子模块是可独立启动的 Spring Boot 应用，示例代码在 `src/main/java`，测试在 `src/test/java`。
- 端口默认 `0`（随机），需要固定时用 `SERVER_PORT`，示例只用偏门端口（18761–18766）。
- 连接地址通过 `application.yml` 的 `${环境变量:默认值}` 占位符配置，不在代码或测试里写死。
- 测试优先使用内嵌基础设施：Kafka 用 `EmbeddedKafka`，RocketMQ 用 Testcontainers。
