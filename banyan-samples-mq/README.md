# banyan-samples-mq

`banyan-mq` 的使用示例，每个模块可单独启动、单独测试。

| 模块 | 场景 | 默认端口 | 测试基础设施 |
|---|---|---|---|
| [`banyan-samples-mq-kafka-producer`](banyan-samples-mq-kafka-producer/README.md) | topic 直发、key + headers、payload + 自定义 codec | 随机（示例 18761） | `EmbeddedKafka` |
| [`banyan-samples-mq-kafka-consumer`](banyan-samples-mq-kafka-consumer/README.md) | 消费上下文、boolean 返回 | 随机（示例 18762） | `EmbeddedKafka` |
| [`banyan-samples-mq-rocketmq-producer`](banyan-samples-mq-rocketmq-producer/README.md) | tag、延迟、key + headers、payload、事务提交后发送 | 随机（示例 18763） | Testcontainers |
| [`banyan-samples-mq-rocketmq-consumer`](banyan-samples-mq-rocketmq-consumer/README.md) | tag 过滤、消费上下文、boolean 返回 | 随机（示例 18764） | Testcontainers |
| [`banyan-samples-mq-multi-backend`](banyan-samples-mq-multi-backend/README.md) | Kafka 与 RocketMQ 并存，两个生产者按实例路由 | 随机（示例 18765） | 不连 broker，只验证实例映射 |
| [`banyan-samples-mq-payload-types`](banyan-samples-mq-payload-types/README.md) | String / Integer / Boolean / POJO / List / Map / `Map<Bean, List<Bean>>` | 随机（示例 18766） | Testcontainers |

所有应用 `server.port` 默认 `0`（每次随机），启动日志会打印实际端口；需要固定时用 `SERVER_PORT` 指定，示例只用偏门端口。

## 能力速查

| 能力 | 示例模块 |
|---|---|
| 基础发送（topic + body） | `kafka-producer`、`rocketmq-producer` |
| tag / 路由键 | `rocketmq-producer`、`rocketmq-consumer` |
| 延迟消息 | `rocketmq-producer` |
| key + headers | `kafka-producer`、`rocketmq-producer` |
| 事务提交后发送 | `rocketmq-producer` |
| 自定义编解码 | `kafka-producer`、`payload-types` |
| 消费上下文（messageId / retryTimes 等） | `kafka-consumer`、`rocketmq-consumer` |
| `boolean` 控制重试 | `kafka-consumer`、`rocketmq-consumer` |
| 注解属性用 Spring 占位符 | `rocketmq-consumer` |
| 同一应用多后端 | `multi-backend` |
| payload 类型矩阵 | `payload-types` |

### 消费上下文

消费方法可以声明可选的第二个参数 `MqConsumeContext`，从里面取 `messageId`：

- 框架统一命名为 `messageId`。Kafka 没有原生消息 id，这里是 `topic-partition-offset` 拼出的定位串；
  RocketMQ 取 `MessageExt#getMsgId()`。
- Kafka 的 `offset` 是分区内索引，不是全局 id。要读原始分区号与 offset，需把
  `context.getNativeMessage()` 强转为 `ConsumerRecord`。

`kafka-consumer` 与 `rocketmq-consumer` 把 `context.getMessageId()` 记录进 `MessageStore`，
可用 `GET /messages/ids` 查看；各自的 `capturesMessageIdFromConsumeContext` 用例断言
messageId 来自消费上下文。`kafka-consumer` 另有 `readsPartitionAndOffsetFromNativeMessage`
演示分区号与 offset，结果可用 `GET /messages/positions` 查看。

### payload 类型矩阵

`payload-types` 覆盖 7 种形态：

| 类型 | 消费端转换 |
|---|---|
| `String` | `toBean(String.class)` |
| `Integer` / `Boolean` | `toBean(Integer.class)` |
| POJO | `toBean(Order.class)` |
| `List<Bean>` | `toBean(new TypeReference<List<Order>>(){}.getType())` |
| `Map<String, Bean>` | `toBean(new TypeReference<Map<String,Order>>(){}.getType())` |
| `Map<Bean, List<Bean>>` | 需 KeySerializer / KeyDeserializer，见子模块 README |

`String` 直通、其余走 Jackson；`Map<Bean, List<Bean>>` 的完整原理与替代建议见
[`payload-types/README.md`](banyan-samples-mq-payload-types/README.md)。

### 多后端

`multi-backend` 有两个 Kafka 集群，`banyan.mq.instances.<name>.type` 只能区分后端类型、区分不了同类型的不同连接，所以在 `config` 包里用 Java 显式创建三个实例：

- `member-kafka`、`payment-kafka`：各自一套 `KafkaTemplate` / `ConsumerFactory` / `KafkaMqBackend`
- `member-rocketmq`：RocketMQ 自动装配提供的后端

`application.yml` 里只关闭 Kafka 的自动装配（`banyan.mq.kafka.enabled=false`），不声明 `instances`：

```yaml
banyan:
  mq:
    kafka:
      enabled: false
```

三个 `MqProducer` 分别绑定上述实例，业务接口按需选择。测试不连真实 broker，只验证实例映射与路由。

## 运行单个示例

```bash
# Kafka 生产者
SERVER_PORT=18761 KAFKA_BOOTSTRAP_SERVERS=127.0.0.1:9092 \
  mvn -pl banyan-samples-mq/banyan-samples-mq-kafka-producer spring-boot:run

# RocketMQ 消费者
SERVER_PORT=18764 ROCKETMQ_NAME_SERVER=127.0.0.1:9876 \
  mvn -pl banyan-samples-mq/banyan-samples-mq-rocketmq-consumer spring-boot:run

# 多后端
SERVER_PORT=18765 KAFKA_BOOTSTRAP_SERVERS=127.0.0.1:9092 ROCKETMQ_NAME_SERVER=127.0.0.1:9876 \
  mvn -pl banyan-samples-mq/banyan-samples-mq-multi-backend spring-boot:run

# payload 类型矩阵
SERVER_PORT=18766 ROCKETMQ_NAME_SERVER=127.0.0.1:9876 \
  mvn -pl banyan-samples-mq/banyan-samples-mq-payload-types spring-boot:run
```

## 测试用例索引

| 模块 | 测试类 | 方法 | 场景 |
|---|---|---|---|
| `kafka-producer` | `KafkaProducerSampleTest` | `sendsMessageThroughHttp` | 只给 topic + body |
| | | `sendsKeyAndHeaders` | 指定 key 与 headers |
| | | `encodesPayloadWithCustomCodec` | payload + 自定义编解码 |
| `kafka-consumer` | `KafkaConsumerSampleTest` | `consumesWithMqConsumer` | 消费 + 上下文 + 业务 Service 调用 |
| | | `capturesMessageIdFromConsumeContext` | 从消费上下文取 messageId，校验 `topic-partition-offset` 格式 |
| | | `readsPartitionAndOffsetFromNativeMessage` | 强转 `ConsumerRecord`，取分区号与 offset 并与 messageId 对齐 |
| `rocketmq-producer` | `RocketMqProducerSampleTest` | `sendsPlainAndDelayedMessages` | 普通发送 + 延迟发送 |
| | | `sendsKeyedPayloadAndAfterCommitMessages` | key/headers、payload、事务提交后发送 |
| `rocketmq-consumer` | `RocketMqConsumerSampleTest` | `consumesWithMqConsumer` | tag 过滤 + 消费上下文 |
| | | `resolvesPlaceholderAttributesAndFiltersTag` | 注解属性经 Spring 占位符解析后生效 |
| | | `capturesMessageIdFromConsumeContext` | 从消费上下文取 messageId，校验非空 |
| `multi-backend` | `MultiBackendRoutingTest` | `registersTwoLogicalInstances` | 两个逻辑实例的映射与路由 |
| | `MultiKafkaConsumerTest` | `registersConsumersToDifferentInstances` | 两个 Kafka 消费者分别注册到不同实例 |
| `payload-types` | `PayloadTypesSampleTest` | `supportsAllPayloadTypes` | String / Integer / Boolean / POJO / List / Map / Map<Bean,List<Bean>> |

## 测试

```bash
# 全部（改过 banyan-mq 后用 clean，避免旧 class）
mvn -pl banyan-samples-mq -am clean test

# 单个
mvn -pl banyan-samples-mq/banyan-samples-mq-kafka-producer -am clean test
```

- Kafka 用例使用 `EmbeddedKafka`，不需要 Docker。
- RocketMQ 用例使用 Testcontainers 启动 `apache/rocketmq:5.3.1`（arm64），需要 Docker；无 Docker 时自动跳过。
- `multi-backend` 测试用假后端验证实例映射与路由，不连真实 broker。
- 测试中不写死任何本地地址：broker 地址由测试基础设施注入，业务配置只保留 `${...:默认值}` 占位符。

## 依赖

所有示例依赖发布到本地仓库的 `cn.kennylee.banyan:banyan-mq-*:0.1.0-SNAPSHOT`。修改 banyan-mq 后先执行：

```bash
mvn -pl banyan-mq/banyan-mq-core,banyan-mq/banyan-mq-commons-spring,banyan-mq/banyan-mq-producer,banyan-mq/banyan-mq-consumer,banyan-mq/banyan-mq-kafka-spring,banyan-mq/banyan-mq-rocketmq-apache -am install -DskipTests
```
