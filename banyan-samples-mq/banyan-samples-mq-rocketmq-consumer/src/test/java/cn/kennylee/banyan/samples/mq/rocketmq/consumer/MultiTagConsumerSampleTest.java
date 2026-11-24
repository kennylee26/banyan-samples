package cn.kennylee.banyan.samples.mq.rocketmq.consumer;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 多 tag 表达式端到端测试：一个方法订阅 created 与 cancelled，同一 topic 的其它 tag 不消费。
 *
 * @author kennylee
 * @since 0.1.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
class MultiTagConsumerSampleTest {

    @Container
    static final RocketMqContainers RMQ =
            new RocketMqContainers(MultiTagSampleConsumer.TOPIC);

    @DynamicPropertySource
    static void rocketMqProperties(DynamicPropertyRegistry registry) {
        registry.add("rocketmq.name-server", RMQ::nameServer);
    }

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private RocketMQTemplate template;

    @Autowired
    private MessageStore store;

    @AfterEach
    void clearStore() {
        store.clear();
    }

    /** 多 tag 表达式同时接收两个 tag，不接收其它 tag。 */
    @Test
    void receivesAllTagsInExpression() {
        // when
        template.syncSend(MultiTagSampleConsumer.TOPIC + ":created",
                "{\"id\":\"multi-created\"}");
        template.syncSend(MultiTagSampleConsumer.TOPIC + ":cancelled",
                "{\"id\":\"multi-cancelled\"}");
        template.syncSend(MultiTagSampleConsumer.TOPIC + ":other",
                "{\"id\":\"multi-other\"}");

        // then
        await().atMost(40, TimeUnit.SECONDS).until(() ->
                messages().contains("created:{\"id\":\"multi-created\"}")
                        && messages().contains("cancelled:{\"id\":\"multi-cancelled\"}"));
        assertTrue(messages().contains("created:{\"id\":\"multi-created\"}"));
        assertTrue(messages().contains("cancelled:{\"id\":\"multi-cancelled\"}"));
        assertFalse(messages().contains("other:{\"id\":\"multi-other\"}"));
    }

    private List<String> messages() {
        String[] body = rest.getForObject("/messages", String[].class);
        return body == null ? List.of() : List.of(body);
    }
}
