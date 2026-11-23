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

已消费消息的 messageId：

```bash
curl http://localhost:18762/messages/ids
```

已消费消息的分区号与 offset：

```bash
curl http://localhost:18762/messages/positions
```

## 消费上下文

Kafka 没有原生消息 id。`context.getMessageId()` 是框架用 `topic-partition-offset` 拼出的定位串；
常规字段不需要接触 Kafka 类型，要读原始分区号与 offset 时需强转 `nativeMessage`：

```java
@MqConsumer(instance = "kafka", topic = TOPIC, groupSuffixEnabled = false, tagSuffixEnabled = false)
public boolean onMessage(MqMessage message, MqConsumeContext context) {
    String messageId = context.getMessageId(); // 如 banyan-sample-kafka-consumer-0-3
    ConsumerRecord<?, ?> record = (ConsumerRecord<?, ?>) context.getNativeMessage();
    ConsumedPosition position = new ConsumedPosition(
            messageId, record.partition(), record.offset());
    orderService.handle(message.getBody());
    store.add(message.getBody(), position);
    return true;
}
```

`offset` 是分区内单调递增的索引，不是全局 id，跨分区不唯一；`partition` / `offset`
可通过 `GET /messages/positions` 查询。

## 测试用例

| 方法 | 场景 |
|---|---|
| `consumesWithMqConsumer` | 消费 + `MqConsumeContext` + 注入的业务 Service 被调用 |
| `capturesMessageIdFromConsumeContext` | 从消费上下文取 messageId，校验 `topic-partition-offset` 格式 |
| `readsPartitionAndOffsetFromNativeMessage` | 强转 `ConsumerRecord`，取分区号与 offset 并与 messageId 对齐 |

## 测试

```bash
mvn -pl banyan-samples-mq/banyan-samples-mq-kafka-consumer -am test
```

使用 `EmbeddedKafka`，不需要外部 Kafka。
