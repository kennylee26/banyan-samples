# banyan-samples-mq-multi-backend

演示同一应用同时使用多个后端实例：

- **两个 Kafka 集群**：`member` 与 `payment`
- **一个 RocketMQ**：`member-rocketmq`

生产者分别绑定实例，消费者按实例名分别消费。

## 为什么不能只靠 application.yml

`banyan.mq.instances.<name>.type` 只能区分**后端类型**，区分不了「同类型的不同连接」。两个 Kafka 集群需要：

1. 各自一套 `KafkaTemplate` / `ConsumerFactory`
2. 各自一个 `KafkaMqBackend`
3. 显式声明 `MqInstanceRegistry`，把实例名映射到对应后端

这些都在 `config` 包下的 `KafkaClustersConfig` 与 `MqInstanceRegistryConfig` 里。

## 目录结构

```
src/main/java/cn/kennylee/banyan/samples/mq/multi/
├── MultiBackendSampleApplication.java
├── config/                            # 配置类集中在这里
│   ├── KafkaClustersConfig.java       # 两个 Kafka 集群的 Template / ConsumerFactory / 后端
│   ├── MqInstanceRegistryConfig.java  # 实例名 → 后端实现 的映射
│   └── MqProducerConfig.java          # 三个分别绑定实例的生产者
├── MemberKafkaConsumer.java
├── PaymentKafkaConsumer.java
├── MessageController.java
├── MessageQueryController.java
├── MessageRequest.java
└── MessageStore.java
```

业务代码只依赖 `MqProducer` / `@MqConsumer` 和实例名，连接细节全部落在 `config` 包里。

## 配置

```yaml
samples:
  kafka:
    member:
      bootstrap-servers: ${MEMBER_KAFKA_SERVERS:localhost:9092}
    payment:
      bootstrap-servers: ${PAYMENT_KAFKA_SERVERS:localhost:9094}

rocketmq:
  name-server: ${ROCKETMQ_NAME_SERVER:localhost:9876}

banyan:
  mq:
    # 关闭 Kafka 后端的自动装配：两个 Kafka 集群由 KafkaClustersConfig 手动创建
    kafka:
      enabled: false
```

| 配置 | 说明 | 默认 |
|---|---|---|
| `SERVER_PORT` | 应用端口 | `0`（随机） |
| `MEMBER_KAFKA_SERVERS` | member Kafka 地址 | `localhost:9092` |
| `PAYMENT_KAFKA_SERVERS` | payment Kafka 地址 | `localhost:9094` |
| `ROCKETMQ_NAME_SERVER` | RocketMQ nameserver 地址 | `localhost:9876` |

## 启动

```bash
SERVER_PORT=18765 \
MEMBER_KAFKA_SERVERS=127.0.0.1:9092 \
PAYMENT_KAFKA_SERVERS=127.0.0.1:9094 \
ROCKETMQ_NAME_SERVER=127.0.0.1:9876 \
mvn spring-boot:run
```

`server.port` 默认 `0`（随机），启动日志会打印实际端口；需要固定时用 `SERVER_PORT`，示例端口 18765。

## 使用

```bash
# member Kafka
curl -X POST http://localhost:18765/messages/member-kafka \
  -H 'Content-Type: application/json' -d '{"payload":"{\"id\":1}"}'

# payment Kafka
curl -X POST http://localhost:18765/messages/payment-kafka \
  -H 'Content-Type: application/json' -d '{"payload":"{\"id\":2}"}'

# RocketMQ
curl -X POST http://localhost:18765/messages/rocketmq \
  -H 'Content-Type: application/json' -d '{"payload":"{\"id\":3}"}'

# 查看两个 Kafka 集群的消费结果
curl http://localhost:18765/messages
```

消费者：

- `MemberKafkaConsumer` → `instance = "member-kafka"`
- `PaymentKafkaConsumer` → `instance = "payment-kafka"`

两个消费者方法都只写方法级注解，由 `MqConsumerRegistrar` 按实例名注册到不同后端。

## 测试用例

| 测试类 / 方法 | 场景 |
|---|---|
| `MultiBackendRoutingTest#registersTwoLogicalInstances` | 两个逻辑实例的映射与路由 |
| `MultiKafkaConsumerTest#registersConsumersToDifferentInstances` | 两个消费者分别注册到 `member-kafka` / `payment-kafka` |

## 测试

```bash
mvn -pl banyan-samples-mq/banyan-samples-mq-multi-backend -am clean test
```

两个测试都不连真实 broker。
