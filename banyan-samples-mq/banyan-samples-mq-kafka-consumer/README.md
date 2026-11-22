# banyan-samples-mq-kafka-consumer

演示用 `@MqConsumer` 消费 Kafka 消息，一个 class 里可以写多个消费者方法。

## 启动

```bash
SERVER_PORT=18762 mvn spring-boot:run
```

`server.port` 默认 `0`（随机），启动日志会打印实际端口；需要固定时用 `SERVER_PORT`，示例端口 18762。

## 配置

| 配置 | 说明 | 默认 |
|---|---|---|
| `SERVER_PORT` | 应用端口 | `0`（随机） |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka 地址 | `localhost:9092` |

## 使用

向 `banyan-sample-kafka-consumer` 发送消息后，查询已消费内容：

```bash
curl http://localhost:18762/messages
```

## 测试用例

| 方法 | 场景 |
|---|---|
| `consumesWithMqConsumer` | 消费 + `MqConsumeContext` + 注入的业务 Service 被调用 |

## 测试

```bash
mvn -pl banyan-samples-mq/banyan-samples-mq-kafka-consumer -am test
```

使用 `EmbeddedKafka`，不需要外部 Kafka。
