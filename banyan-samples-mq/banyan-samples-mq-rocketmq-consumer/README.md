# banyan-samples-mq-rocketmq-consumer

演示用 `@MqConsumer` 消费 RocketMQ 消息，按 tag 过滤。

## 启动

```bash
SERVER_PORT=18764 ROCKETMQ_NAME_SERVER=127.0.0.1:9876 mvn spring-boot:run
```

`server.port` 默认 `0`（随机），启动日志会打印实际端口；需要固定时用 `SERVER_PORT`，示例端口 18764。

## 配置

| 配置 | 说明 | 默认 |
|---|---|---|
| `SERVER_PORT` | 应用端口 | `0`（随机） |
| `ROCKETMQ_NAME_SERVER` | RocketMQ nameserver 地址 | `localhost:9876` |

## 使用

向 `banyan-sample-rocketmq-consumer` 的 `paid` tag 发送消息后，查询已消费内容：

```bash
curl http://localhost:18764/messages
```

## 测试用例

| 方法 | 场景 |
|---|---|
| `consumesWithMqConsumer` | tag 过滤 + 消费上下文 |

## 测试

```bash
mvn -pl banyan-samples-mq/banyan-samples-mq-rocketmq-consumer -am test
```

测试用 Testcontainers 启动 `apache/rocketmq:5.3.1`，需要 Docker；没有 Docker 时自动跳过。
