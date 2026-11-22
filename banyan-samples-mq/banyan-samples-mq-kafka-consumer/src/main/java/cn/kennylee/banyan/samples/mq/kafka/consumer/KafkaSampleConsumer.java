package cn.kennylee.banyan.samples.mq.kafka.consumer;

import cn.kennylee.banyan.mq.consumer.MqConsumer;
import cn.kennylee.banyan.mq.core.MqConsumeContext;
import cn.kennylee.banyan.mq.core.MqMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 消费者示例：方法级注解，业务代码不出现 Kafka 类型。
 *
 * @author kennylee
 * @since 0.1.0
 */
@Slf4j
@Component
public class KafkaSampleConsumer {

    /** 消费 topic。 */
    static final String TOPIC = "banyan-sample-kafka-consumer";

    private final MessageStore store;

    private final OrderService orderService;

    /**
     * 构造消费者。
     *
     * @param store        消息存储
     * @param orderService 业务服务
     */
    public KafkaSampleConsumer(MessageStore store, OrderService orderService) {
        this.store = store;
        this.orderService = orderService;
    }

    /**
     * 消费入口：返回 true 表示成功，返回 false 触发后端重试。
     *
     * @param message 消息
     * @return true 表示消费成功
     */
    @MqConsumer(instance = "kafka", topic = TOPIC, groupSuffixEnabled = false, tagSuffixEnabled = false)
    public boolean onMessage(MqMessage message, MqConsumeContext context) {
        log.debug("收到消息: topic={}, messageId={}, retryTimes={}",
                context.getTopic(), context.getMessageId(), context.getRetryTimes());
        orderService.handle(message.getBody());
        store.add(message.getBody());
        return true;
    }
}
