package cn.kennylee.banyan.samples.mq.rocketmq.consumer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 多 tag 数组端到端测试：一个方法订阅 created 与 cancelled，同一 topic 的其它 tag 不消费。
 *
 * @author kennylee
 * @since 0.1.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
class MultiTagConsumerSampleTest extends AbstractRocketMqSampleTest {

    /** 多 tag 数组同时接收两个 tag，不接收其它 tag。 */
    @Test
    void receivesAllConfiguredTags() {
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
        await().during(2, TimeUnit.SECONDS).atMost(5, TimeUnit.SECONDS)
                .until(() -> !messages().contains("other:{\"id\":\"multi-other\"}"));
    }
}
