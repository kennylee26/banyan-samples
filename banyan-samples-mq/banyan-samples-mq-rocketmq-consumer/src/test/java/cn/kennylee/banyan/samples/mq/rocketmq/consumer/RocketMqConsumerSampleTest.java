package cn.kennylee.banyan.samples.mq.rocketmq.consumer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RocketMQ 消费者示例测试：原生发送，HTTP 查询消费结果。
 *
 * @author kennylee
 * @since 0.1.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
class RocketMqConsumerSampleTest extends AbstractRocketMqSampleTest {

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
        await().during(2, TimeUnit.SECONDS).atMost(5, TimeUnit.SECONDS)
                .until(() -> !messages().contains("{\"id\":99}"));
    }

    @Test
    void capturesMessageIdFromConsumeContext() {
        // when
        org.apache.rocketmq.client.producer.SendResult sent = template.syncSend(
                RocketMqSampleConsumer.TOPIC + ":" + RocketMqSampleConsumer.TAG, "{\"id\":7}");

        // then
        await().atMost(40, TimeUnit.SECONDS).until(() -> ids().contains(sent.getMsgId()));
        assertTrue(ids().contains(sent.getMsgId()), "消费上下文 messageId 应等于发送结果的 msgId");
    }

    private List<String> ids() {
        String[] ids = rest.getForObject("/messages/ids", String[].class);
        return ids == null ? List.of() : List.of(ids);
    }
}
