package cn.kennylee.banyan.samples.mq.multi;

import cn.kennylee.banyan.mq.consumer.MqConsumer;
import cn.kennylee.banyan.mq.core.MqMessage;
import org.springframework.stereotype.Component;

/**
 * payment Kafka 集群的消费者。
 *
 * @author kennylee
 * @since 0.1.0
 */
@Component
public class PaymentKafkaConsumer {

    private final MessageStore store;

    /**
     * 构造消费者。
     *
     * @param store 记录器
     */
    public PaymentKafkaConsumer(MessageStore store) {
        this.store = store;
    }

    /**
     * 消费入口。
     *
     * @param message 消息
     */
    @MqConsumer(instance = "payment-kafka", topic = "banyan-sample-payment-kafka",
            groupSuffixEnabled = false, tagSuffixEnabled = false)
    public void onMessage(MqMessage message) {
        store.add("payment:" + message.getBody());
    }
}
