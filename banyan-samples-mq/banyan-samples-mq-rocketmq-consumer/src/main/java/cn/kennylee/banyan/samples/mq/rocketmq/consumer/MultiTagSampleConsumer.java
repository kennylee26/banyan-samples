package cn.kennylee.banyan.samples.mq.rocketmq.consumer;

import cn.kennylee.banyan.mq.consumer.MqConsumer;
import cn.kennylee.banyan.mq.core.MqConsumeContext;
import cn.kennylee.banyan.mq.core.MqMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 多 tag 表达式消费者：一个方法订阅同一 topic 的 created 与 cancelled 两个 tag。
 *
 * @author kennylee
 * @since 0.1.0
 */
@Slf4j
@Component
public class MultiTagSampleConsumer {

    /** 消费 topic，与单 tag 示例共用。 */
    static final String TOPIC = "banyan-sample-rocketmq-consumer";

    private final MessageStore store;

    /**
     * 构造消费者。
     *
     * @param store 消息存储
     */
    public MultiTagSampleConsumer(MessageStore store) {
        this.store = store;
    }

    /**
     * 接收 created 或 cancelled 的消息。
     *
     * @param message 消息
     * @param context 消费上下文
     */
    @MqConsumer(
            instance = "${samples.mq.instance:rocketmq}",
            topic = "${samples.mq.topic:" + TOPIC + "}",
            tag = {"created", "cancelled"},
            group = "banyan-sample-rocketmq-multi-tag",
            groupSuffixEnabled = false,
            tagSuffixEnabled = false)
    public void onStatusChanged(MqMessage message, MqConsumeContext context) {
        log.debug("收到多 tag 消息: tag={}, body={}", context.getTag(), message.getBody());
        store.add(context.getMessageId(), context.getTag() + ":" + message.getBody());
    }
}
