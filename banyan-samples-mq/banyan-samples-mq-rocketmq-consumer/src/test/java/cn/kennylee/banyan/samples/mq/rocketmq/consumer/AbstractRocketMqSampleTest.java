package cn.kennylee.banyan.samples.mq.rocketmq.consumer;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;

import java.util.List;

/**
 * RocketMQ 样例测试基类：共享容器、HTTP 查询与消息清理。
 *
 * @author kennylee
 * @since 0.1.0
 */
abstract class AbstractRocketMqSampleTest {

    @Container
    static final RocketMqContainers RMQ =
            new RocketMqContainers(MultiTagSampleConsumer.TOPIC);

    @DynamicPropertySource
    static void rocketMqProperties(DynamicPropertyRegistry registry) {
        registry.add("rocketmq.name-server", RMQ::nameServer);
    }

    @Autowired
    protected TestRestTemplate rest;

    @Autowired
    protected RocketMQTemplate template;

    @Autowired
    protected MessageStore store;

    @AfterEach
    void clearStore() {
        store.clear();
    }

    protected List<String> messages() {
        String[] body = rest.getForObject("/messages", String[].class);
        return body == null ? List.of() : List.of(body);
    }
}
