package cn.kennylee.banyan.samples.mq.payload;

import cn.kennylee.banyan.mq.consumer.MqConsumer;
import cn.kennylee.banyan.mq.core.MqMessage;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 按 tag 演示各种 payload 类型的消费与转换。
 *
 * @author kennylee
 * @since 0.1.0
 */
@Component
public class PayloadConsumer {

    /** 消费 topic。 */
    static final String TOPIC = "banyan-sample-payload";

    private final MessageStore store;

    /**
     * 构造消费者。
     *
     * @param store 记录器
     */
    public PayloadConsumer(MessageStore store) {
        this.store = store;
    }

    /**
     * 基础类型 String。
     *
     * @param message 消息
     */
    @MqConsumer(topic = TOPIC, tag = "string", groupSuffixEnabled = false, tagSuffixEnabled = false)
    public void onString(MqMessage message) {
        store.add("string:" + message.getData().toBean(String.class));
    }

    /**
     * 基础类型 Integer。
     *
     * @param message 消息
     */
    @MqConsumer(topic = TOPIC, tag = "int", groupSuffixEnabled = false, tagSuffixEnabled = false)
    public void onInteger(MqMessage message) {
        store.add("int:" + message.getData().toBean(Integer.class));
    }

    /**
     * 基础类型 Boolean。
     *
     * @param message 消息
     */
    @MqConsumer(topic = TOPIC, tag = "boolean", groupSuffixEnabled = false, tagSuffixEnabled = false)
    public void onBoolean(MqMessage message) {
        store.add("boolean:" + message.getData().toBean(Boolean.class));
    }

    /**
     * POJO。
     *
     * @param message 消息
     */
    @MqConsumer(topic = TOPIC, tag = "pojo", groupSuffixEnabled = false, tagSuffixEnabled = false)
    public void onPojo(MqMessage message) {
        Order order = message.getData().toBean(Order.class);
        store.add("pojo:" + order.id());
    }

    /**
     * List&lt;Bean&gt;。
     *
     * @param message 消息
     */
    @MqConsumer(topic = TOPIC, tag = "list", groupSuffixEnabled = false, tagSuffixEnabled = false)
    public void onList(MqMessage message) {
        List<Order> orders = message.getData()
                .toBean(new TypeReference<List<Order>>() {
                }.getType());
        store.add("list:" + orders.size());
    }

    /**
     * Map&lt;String, Bean&gt;。
     *
     * @param message 消息
     */
    @MqConsumer(topic = TOPIC, tag = "map", groupSuffixEnabled = false, tagSuffixEnabled = false)
    public void onMap(MqMessage message) {
        Map<String, Order> orders = message.getData()
                .toBean(new TypeReference<Map<String, Order>>() {
                }.getType());
        store.add("map:" + orders.size());
    }

    /**
     * Map&lt;Bean, List&lt;Bean&gt;&gt;：依赖自定义 KeyDeserializer。
     *
     * @param message 消息
     */
    @MqConsumer(topic = TOPIC, tag = "map-bean", groupSuffixEnabled = false, tagSuffixEnabled = false)
    public void onMapBean(MqMessage message) {
        Map<OrderKey, List<Order>> orders = message.getData()
                .toBean(new TypeReference<Map<OrderKey, List<Order>>>() {
                }.getType());
        store.add("map-bean:" + orders.size());
    }
}
