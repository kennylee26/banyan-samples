package cn.kennylee.banyan.samples.mq.kafka.producer;

import cn.kennylee.banyan.mq.core.spi.MqSendRequest;
import cn.kennylee.banyan.mq.producer.MqProducer;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 发送入口：演示 topic / key / headers / 自定义编解码。
 *
 * @author kennylee
 * @since 0.1.0
 */
@RestController
@RequestMapping("/messages")
public class MessageController {

    /** 目标 topic。 */
    static final String TOPIC = "banyan-sample-kafka-producer";

    private final MqProducer producer;

    /**
     * 构造控制器。
     *
     * @param producer 发送门面
     */
    public MessageController(MqProducer producer) {
        this.producer = producer;
    }

    /**
     * 只给 topic 与 body。
     *
     * @param request 请求体
     */
    @PostMapping
    public void send(@RequestBody MessageRequest request) {
        producer.send(TOPIC, request.payload());
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
                .payload(new Payload(request.payload()))
                .build());
    }
}
