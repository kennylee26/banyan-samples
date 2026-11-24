# banyan-samples-mq-rocketmq-consumer

演示用 `@MqConsumer` 消费 RocketMQ 消息，按 tag 过滤；单 tag 消费者的 `instance` / `topic` / `tag` / `group` 写成 Spring 占位符，多 tag 消费者直接用数组。

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

注解占位符的取值在 `application.yml`：

```yaml
samples:
  mq:
    instance: rocketmq
    topic: banyan-sample-rocketmq-consumer
    tag: paid
    group: banyan-sample-rocketmq-consumer
```

```java
@MqConsumer(
        instance = "${samples.mq.instance:rocketmq}",
        topic = "${samples.mq.topic:banyan-sample-rocketmq-consumer}",
        tag = "${samples.mq.tag:paid}",
        group = "${samples.mq.group:banyan-sample-rocketmq-consumer}")
```

`concurrency` / `maxRetryTimes` 是数值属性，注解上不能写占位符。

## 使用

向 `banyan-sample-rocketmq-consumer` 的 `paid` tag 发送消息后，查询已消费内容。
同一个 topic 上还有一个多 tag 消费者 `MultiTagSampleConsumer`，用 `tag = {"created", "cancelled"}` 订阅两个 tag，记录内容带 `tag:` 前缀：

```bash
curl http://localhost:18764/messages
```

已消费消息的 messageId：

```bash
curl http://localhost:18764/messages/ids
```

## 消费上下文

`messageId` 从方法第二个参数 `MqConsumeContext` 取，值来自 RocketMQ 的 `MessageExt#getMsgId()`：

```java
@MqConsumer(instance = "${samples.mq.instance:rocketmq}",
        topic = "${samples.mq.topic:banyan-sample-rocketmq-consumer}", ...)
public boolean onPaid(MqMessage message, MqConsumeContext context) {
    String messageId = context.getMessageId();
    store.add(messageId, message.getBody());
    return true;
}
```

## 测试用例

| 方法 | 场景 |
|---|---|
| `consumesWithMqConsumer` | tag 过滤 + 消费上下文 |
| `MultiTagConsumerSampleTest#receivesAllTagsInExpression` | 一个方法用 `tag = {"created", "cancelled"}` 订阅同一 topic 的多个 tag，其它 tag 不消费 |
| `resolvesPlaceholderAttributesAndFiltersTag` | 注解属性经 Spring 占位符解析后生效：topic 对上、tag 过滤仍生效 |
| `capturesMessageIdFromConsumeContext` | 从消费上下文取 messageId，校验非空 |

## 测试

```bash
mvn -pl banyan-samples-mq/banyan-samples-mq-rocketmq-consumer -am test
```

测试用 Testcontainers 启动 `apache/rocketmq:5.3.1`，需要 Docker；没有 Docker 时自动跳过。
