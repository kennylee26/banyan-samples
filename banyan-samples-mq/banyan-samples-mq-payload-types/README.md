# banyan-samples-mq-payload-types

演示 `MqSendRequest.payload` 支持的各种类型，以及消费端如何用 `MqData.toBean(...)` 转回业务对象。

## 为什么需要这个示例

`payload` 声明为 `Object`，最终由 `MqMessageCodec` 决定怎么转成消息体。默认的 `JsonMqMessageCodec` 委托 `JsonCodec`（Jackson），规则是：

- `String` **直通**，视为已经是 JSON 文本，不再二次序列化
- 其余类型交给 Jackson：基础类型、POJO、集合、Map 都按 Jackson 的规则处理

不同形态的边界容易踩坑（例如 String 被包成带引号的字符串、Map 的 Bean key 无法直接转 JSON），这个模块把每种类型都跑一遍，并给出消费端转回业务对象的写法。

## 类型矩阵

| 类型 | 发送代码 | 消费端转换 | 说明 |
|---|---|---|---|
| `String` | `producer.send(TOPIC, "string", "hello")` | `toBean(String.class)` | 直通，body 就是 `hello` |
| `Integer` | `producer.send(TOPIC, "int", 123)` | `toBean(Integer.class)` | body 为 `123` |
| `Boolean` | `producer.send(TOPIC, "boolean", true)` | `toBean(Boolean.class)` | body 为 `true` |
| POJO | `producer.send(TOPIC, "pojo", new Order("o1", "n1"))` | `toBean(Order.class)` | body 为 JSON 对象 |
| `List<Bean>` | `producer.send(TOPIC, "list", List.of(order))` | `toBean(new TypeReference<List<Order>>(){}.getType())` | body 为 JSON 数组 |
| `Map<String, Bean>` | `producer.send(TOPIC, "map", Map.of("o3", order))` | `toBean(new TypeReference<Map<String,Order>>(){}.getType())` | key 本身是字符串 |
| `Map<Bean, List<Bean>>` | 转成 `Map<OrderKey, List<Order>>` 后发送 | `toBean(new TypeReference<Map<OrderKey,List<Order>>>(){}.getType())` | 需要 KeySerializer / KeyDeserializer |

## Map<Bean, List<Bean>> 的原理

JSON 对象的 key 只能是字符串，而 `Map<OrderKey, List<Order>>` 的 key 是对象，所以需要两步：

1. **序列化**：`OrderKeySerializer` 把 `OrderKey` 写成它的 `id`
2. **反序列化**：`OrderKeyDeserializer` 从字符串还原 `OrderKey`

在 `JsonCodecConfig` 中注册：

```java
SimpleModule module = new SimpleModule()
        .addKeySerializer(OrderKey.class, new OrderKeySerializer())
        .addKeyDeserializer(OrderKey.class, new OrderKeyDeserializer());

ObjectMapper mapper = JsonMapper.builder()
        .addModule(module)
        .build();

return new JacksonJsonCodec(mapper);
```

发送后的 JSON 形如：

```json
{
  "o4": [
    {"id": "o4", "name": "n4"}
  ]
}
```

**如果不希望引入 key 序列化**，建议改用：

- `List<Order>`，把业务 key 放在每个元素里
- `Map<String, List<Order>>`，key 直接用字符串 id

## 接口

| 接口 | 请求体 | 说明 |
|---|---|---|
| `POST /messages/string` | `{"value":"hello"}` | String |
| `POST /messages/integer` | `{"value":123}` | Integer |
| `POST /messages/boolean` | `{"value":true}` | Boolean |
| `POST /messages/pojo` | `{"id":"o1","name":"n1"}` | POJO |
| `POST /messages/list` | `[{"id":"o2","name":"n2"}]` | `List<Order>` |
| `POST /messages/map` | `{"o3":{"id":"o3","name":"n3"}}` | `Map<String, Order>` |
| `POST /messages/map-bean` | `{"o4":[{"id":"o4","name":"n4"}]}` | `Map<OrderKey, List<Order>>` |

消费端按 tag 分发，每个 tag 对应一种类型，见 `PayloadConsumer`。

## 配置

`application.yml`：

```yaml
rocketmq:
  name-server: ${ROCKETMQ_NAME_SERVER:localhost:9876}
  producer:
    group: banyan-sample-payload

banyan:
  mq:
    instances:
      rocketmq:
        type: rocketmq
```

`JsonCodecConfig` 覆盖默认 `JsonCodec`：

- 注册 `OrderKey` 的 key 序列化
- 启用 `JavaTimeModule`
- 关闭 `FAIL_ON_UNKNOWN_PROPERTIES`

## 启动

```bash
SERVER_PORT=18766 ROCKETMQ_NAME_SERVER=127.0.0.1:9876 mvn spring-boot:run
```

`server.port` 默认 `0`（随机），启动日志会打印实际端口。

## 测试用例

| 方法 | 场景 |
|---|---|
| `supportsAllPayloadTypes` | String / Integer / Boolean / POJO / List / Map / Map<Bean,List<Bean>> 一次全覆盖 |

## 测试

```bash
mvn -pl banyan-samples-mq/banyan-samples-mq-payload-types -am clean test
```

测试用 Testcontainers 启动 `apache/rocketmq:5.3.1`，一次用例发送全部 7 种类型，断言消费端都成功转换。需要 Docker；无 Docker 时自动跳过。

## 注意

- `String` 直通：发送 `"hello"` 时 body 是 `hello`，不是带引号的 `"hello"`
- 未知字段：默认忽略，便于生产方加字段时消费方不崩
- 时间类型：已启用 `JavaTimeModule`，`java.time` 按 ISO 文本处理
- Bean 作 Map key 属于 JSON 的边界用法，能用字符串 key 时优先用字符串
