package cn.kennylee.banyan.samples.mq.payload;

import cn.kennylee.banyan.mq.producer.MqProducer;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 按类型发送消息，演示 payload 的多种形态。
 *
 * @author kennylee
 * @since 0.1.0
 */
@RestController
@RequestMapping("/messages")
public class PayloadController {

    private final MqProducer producer;

    /**
     * 构造控制器。
     *
     * @param producer 发送门面
     */
    public PayloadController(MqProducer producer) {
        this.producer = producer;
    }

    /**
     * String。
     *
     * @param request 请求体
     */
    @PostMapping("/string")
    public void string(@RequestBody StringPayload request) {
        producer.send(PayloadConsumer.TOPIC, "string", request.value());
    }

    /**
     * Integer。
     *
     * @param request 请求体
     */
    @PostMapping("/integer")
    public void integer(@RequestBody IntPayload request) {
        producer.send(PayloadConsumer.TOPIC, "int", request.value());
    }

    /**
     * Boolean。
     *
     * @param request 请求体
     */
    @PostMapping("/boolean")
    public void bool(@RequestBody BoolPayload request) {
        producer.send(PayloadConsumer.TOPIC, "boolean", request.value());
    }

    /**
     * POJO。
     *
     * @param order 订单
     */
    @PostMapping("/pojo")
    public void pojo(@RequestBody Order order) {
        producer.send(PayloadConsumer.TOPIC, "pojo", order);
    }

    /**
     * List&lt;Bean&gt;。
     *
     * @param orders 订单列表
     */
    @PostMapping("/list")
    public void list(@RequestBody List<Order> orders) {
        producer.send(PayloadConsumer.TOPIC, "list", orders);
    }

    /**
     * Map&lt;String, Bean&gt;。
     *
     * @param orders 订单映射
     */
    @PostMapping("/map")
    public void map(@RequestBody Map<String, Order> orders) {
        producer.send(PayloadConsumer.TOPIC, "map", orders);
    }

    /**
     * Map&lt;Bean, List&lt;Bean&gt;&gt;。
     *
     * @param orders 订单映射
     */
    @PostMapping("/map-bean")
    public void mapBean(@RequestBody Map<String, List<Order>> orders) {
        Map<OrderKey, List<Order>> keyed = new LinkedHashMap<>();
        orders.forEach((key, value) -> keyed.put(new OrderKey(key), value));
        producer.send(PayloadConsumer.TOPIC, "map-bean", keyed);
    }
}
