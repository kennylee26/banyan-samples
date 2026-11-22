package cn.kennylee.banyan.samples.mq.rocketmq.consumer;

import cn.kennylee.banyan.mq.consumer.MqConsumer;
import cn.kennylee.banyan.mq.core.MqConsumeContext;
import cn.kennylee.banyan.mq.core.MqMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 消费者示例：方法级注解，按 tag 过滤，并读取消费上下文。
 *
 * @author kennylee
 * @since 0.1.0
 */
@Slf4j
@Component
public class RocketMqSampleConsumer {

    /** 消费 topic。 */
    static final String TOPIC = "banyan-sample-rocketmq-consumer";

    private final MessageStore store;

    /**
     * 构造消费者。
     *
     * @param store 消息存储
     */
    public RocketMqSampleConsumer(MessageStore store) {
        this.store = store;
    }

    /**
     * 消费入口：返回 true 表示成功，返回 false 触发 RocketMQ 重试。
     *
     * @param message 消息
     * @return true 表示消费成功
     */
    @MqConsumer(instance = "rocketmq", topic = TOPIC, tag = "paid", groupSuffixEnabled = false, tagSuffixEnabled = false)
    public boolean onPaid(MqMessage message, MqConsumeContext context) {
        log.debug("收到消息: topic={}, tag={}, messageId={}, retryTimes={}",
                context.getTopic(), context.getTag(), context.getMessageId(),
                context.getRetryTimes());

        store.add(message.getBody());
        return true;
    }
}
