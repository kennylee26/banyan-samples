# banyan-samples-mq-rocketmq-producer

演示用 `MqProducer` 向 RocketMQ 发送普通消息与延迟消息。

## 启动

```bash
SERVER_PORT=18763 ROCKETMQ_NAME_SERVER=127.0.0.1:9876 mvn spring-boot:run
```

`server.port` 默认 `0`（随机），启动日志会打印实际端口；需要固定时用 `SERVER_PORT`，示例端口 18763。

## 配置

| 配置 | 说明 | 默认 |
|---|---|---|
| `SERVER_PORT` | 应用端口 | `0`（随机） |
| `ROCKETMQ_NAME_SERVER` | RocketMQ nameserver 地址 | `localhost:9876` |

## 使用

```bash
# 普通消息
curl -X POST http://localhost:18763/messages \
  -H 'Content-Type: application/json' \
  -d '{"body":"{\"id\":3}"}'

# 延迟 5 秒
curl -X POST http://localhost:18763/messages/delay \
  -H 'Content-Type: application/json' \
  -d '{"body":"{\"id\":4}"}'
```

## 测试用例

| 方法 | 场景 |
|---|---|
| `sendsPlainAndDelayedMessages` | 普通发送 + 延迟发送 |
| `sendsKeyedPayloadAndAfterCommitMessages` | key/headers、payload、事务提交后发送 |

## 测试

```bash
mvn -pl banyan-samples-mq/banyan-samples-mq-rocketmq-producer -am test
```

测试用 Testcontainers 启动 `apache/rocketmq:5.3.1`，需要 Docker；没有 Docker 时自动跳过。
