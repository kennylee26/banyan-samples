package cn.kennylee.banyan.samples.mq.rocketmq.consumer;

import org.apache.rocketmq.client.producer.SendResult;
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
 * RocketMQ 消费者示例测试：原生发送，HTTP 查询消费结果。
 *
 * @author kennylee
 * @since 0.1.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
class RocketMqConsumerSampleTest {

    @Container
    static final RocketMqContainers RMQ = new RocketMqContainers(RocketMqSampleConsumer.TOPIC);

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

    @Test
    void consumesWithMqConsumer() {
        // when
        template.syncSend(RocketMqSampleConsumer.TOPIC + ":" + RocketMqSampleConsumer.TAG,
                "{\"id\":5}");

        // then
        await().atMost(40, TimeUnit.SECONDS).until(() -> messages().contains("{\"id\":5}"));
        assertTrue(messages().contains("{\"id\":5}"));
    }

    @Test
    void resolvesPlaceholderAttributesAndFiltersTag() {
        // when
        template.syncSend(RocketMqSampleConsumer.TOPIC + ":other", "{\"id\":99}");
        template.syncSend(RocketMqSampleConsumer.TOPIC + ":" + RocketMqSampleConsumer.TAG,
                "{\"id\":6}");

        // then
        await().atMost(40, TimeUnit.SECONDS).until(() -> messages().contains("{\"id\":6}"));
        assertFalse(messages().contains("{\"id\":99}"));
    }

    @Test
    void capturesMessageIdFromConsumeContext() {
        // when
        SendResult sent = template.syncSend(
                RocketMqSampleConsumer.TOPIC + ":" + RocketMqSampleConsumer.TAG, "{\"id\":7}");

        // then
        await().atMost(40, TimeUnit.SECONDS).until(() -> ids().contains(sent.getMsgId()));
        assertTrue(ids().contains(sent.getMsgId()), "消费上下文 messageId 应等于发送结果的 msgId");
    }

    private List<String> messages() {
        String[] body = rest.getForObject("/messages", String[].class);
        return body == null ? List.of() : List.of(body);
    }

    private List<String> ids() {
        String[] ids = rest.getForObject("/messages/ids", String[].class);
        return ids == null ? List.of() : List.of(ids);
    }
}
