package cn.kennylee.banyan.samples.mq.rocketmq.producer;

import cn.kennylee.banyan.mq.core.spi.MqSendRequest;
import cn.kennylee.banyan.mq.producer.MqProducer;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;

/**
 * 发送入口：演示 topic / tag / 延迟 / key / headers / 自定义编解码 / 事务提交后发送。
 *
 * @author kennylee
 * @since 0.1.0
 */
@RestController
@RequestMapping("/messages")
public class MessageController {

    /** 目标 topic。 */
    static final String TOPIC = "banyan-sample-rocketmq-producer";

    private final MqProducer producer;

    private final TransactionTemplate transactionTemplate;

    /**
     * 构造控制器。
     *
     * @param producer            发送门面
     * @param transactionTemplate 事务模板
     */
    public MessageController(MqProducer producer, TransactionTemplate transactionTemplate) {
        this.producer = producer;
        this.transactionTemplate = transactionTemplate;
    }

    /**
     * 只给 topic、tag 与 body。
     *
     * @param request 请求体
     */
    @PostMapping
    public void send(@RequestBody MessageRequest request) {
        producer.send(TOPIC, "paid", request.payload());
    }

    /**
     * 发送延迟消息。
     *
     * @param request 请求体
     */
    @PostMapping("/delay")
    public void sendDelay(@RequestBody MessageRequest request) {
        producer.send(TOPIC, "paid", request.payload(), Duration.ofSeconds(5));
    }

    /**
     * 指定 key 与 headers。
     *
     * @param request 请求体
     */
    @PostMapping("/keyed")
    public void sendKeyed(@RequestBody MessageRequest request) {
        producer.send(MqSendRequest.builder()
                .topic(TOPIC)
                .tag("paid")
                .key("order-1")
                .headers(Map.of("tenant", "t1"))
                .payload(request.payload())
                .build());
    }

    /**
     * 只给业务对象，交给自定义编解码器生成 body。
     *
     * @param request 请求体
     */
    @PostMapping("/payload")
    public void sendPayload(@RequestBody MessageRequest request) {
        producer.send(MqSendRequest.builder()
                .topic(TOPIC)
                .tag("paid")
                .payload(new Payload(request.payload()))
                .build());
    }

    /**
     * 本地事务提交后发送。
     *
     * @param request 请求体
     */
    @PostMapping("/after-commit")
    public void sendAfterCommit(@RequestBody MessageRequest request) {
        transactionTemplate.executeWithoutResult(status ->
                producer.sendAfterCommit(TOPIC, "paid", request.payload()));
    }
}
