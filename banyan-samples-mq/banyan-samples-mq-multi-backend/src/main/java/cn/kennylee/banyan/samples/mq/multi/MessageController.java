package cn.kennylee.banyan.samples.mq.multi;

import cn.kennylee.banyan.mq.producer.MqProducer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 按接口区分后端：两个 Kafka 集群 + 一个 RocketMQ 并存。
 *
 * @author kennylee
 * @since 0.1.0
 */
@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MqProducer memberKafkaProducer;

    private final MqProducer paymentKafkaProducer;

    private final MqProducer rocketmqProducer;

    /**
     * 构造控制器。
     *
     * @param memberKafkaProducer  member Kafka 生产者
     * @param paymentKafkaProducer payment Kafka 生产者
     * @param rocketmqProducer     RocketMQ 生产者
     */
    public MessageController(
            @Qualifier("memberKafkaProducer") MqProducer memberKafkaProducer,
            @Qualifier("paymentKafkaProducer") MqProducer paymentKafkaProducer,
            @Qualifier("rocketmqProducer") MqProducer rocketmqProducer) {
        this.memberKafkaProducer = memberKafkaProducer;
        this.paymentKafkaProducer = paymentKafkaProducer;
        this.rocketmqProducer = rocketmqProducer;
    }

    /**
     * 发往 member Kafka。
     *
     * @param request 请求体
     */
    @PostMapping("/member-kafka")
    public void toMemberKafka(@RequestBody MessageRequest request) {
        memberKafkaProducer.send("banyan-sample-member-kafka", request.payload());
    }

    /**
     * 发往 payment Kafka。
     *
     * @param request 请求体
     */
    @PostMapping("/payment-kafka")
    public void toPaymentKafka(@RequestBody MessageRequest request) {
        paymentKafkaProducer.send("banyan-sample-payment-kafka", request.payload());
    }

    /**
     * 发往 RocketMQ。
     *
     * @param request 请求体
     */
    @PostMapping("/rocketmq")
    public void toRocketMq(@RequestBody MessageRequest request) {
        rocketmqProducer.send("banyan-sample-multi-rocketmq", "paid", request.payload());
    }
}
