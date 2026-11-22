# banyan-samples-mq-kafka-producer

演示只用 `MqProducer` 向 Kafka 发送消息，业务代码不出现任何 Kafka 类型。

## 启动

```bash
SERVER_PORT=18761 mvn spring-boot:run
```

`server.port` 默认 `0`（随机），启动日志会打印实际端口；需要固定时用 `SERVER_PORT`，示例端口 18761。

## 配置

| 配置 | 说明 | 默认 |
|---|---|---|
| `SERVER_PORT` | 应用端口 | `0`（随机） |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka 地址 | `localhost:9092` |

## 使用

```bash
curl -X POST http://localhost:18761/messages \
  -H 'Content-Type: application/json' \
  -d '{"body":"{\"id\":1}"}'
```

## 测试用例

| 方法 | 场景 |
|---|---|
| `sendsMessageThroughHttp` | 只给 topic + body |
| `sendsKeyAndHeaders` | 指定 key 与 headers |
| `encodesPayloadWithCustomCodec` | payload + 自定义编解码 |

## 测试

```bash
mvn -pl banyan-samples-mq/banyan-samples-mq-kafka-producer -am test
```

使用 `EmbeddedKafka`，不需要外部 Kafka。
