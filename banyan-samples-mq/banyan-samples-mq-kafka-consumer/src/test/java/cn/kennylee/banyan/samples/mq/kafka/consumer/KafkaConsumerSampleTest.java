package cn.kennylee.banyan.samples.mq.kafka.consumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Kafka 消费者示例测试：原生发送，HTTP 查询消费结果。
 *
 * @author kennylee
 * @since 0.1.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(partitions = 1, topics = KafkaSampleConsumer.TOPIC)
class KafkaConsumerSampleTest {

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private KafkaTemplate<String, String> template;

    @Autowired
    private OrderService orderService;

    @Autowired
    private MessageStore store;

    @AfterEach
    void clearRecords() {
        store.clear();
        orderService.clear();
    }

    @Test
    void consumesWithMqConsumer() {
        // when
        template.send(KafkaSampleConsumer.TOPIC, "{\"id\":2}");

        // then
        await().atMost(15, TimeUnit.SECONDS).until(() -> !messages().isEmpty());
        assertEquals(List.of("{\"id\":2}"), messages());
        assertEquals(List.of("{\"id\":2}"), orderService.handled());
    }

    @Test
    void capturesMessageIdFromConsumeContext() {
        // when
        template.send(KafkaSampleConsumer.TOPIC, "{\"id\":3}");

        // then
        await().atMost(15, TimeUnit.SECONDS).until(() -> !ids().isEmpty());
        assertTrue(ids().stream().anyMatch(id -> id.matches(KafkaSampleConsumer.TOPIC + "-\\d+-\\d+")),
                "messageId 应为 topic-partition-offset，实际: " + ids());
    }

    @Test
    void readsPartitionAndOffsetFromNativeMessage() throws Exception {
        // when
        SendResult<String, String> sent = template.send(KafkaSampleConsumer.TOPIC, "{\"id\":4}")
                .get(10, TimeUnit.SECONDS);
        int expectedPartition = sent.getRecordMetadata().partition();
        long expectedOffset = sent.getRecordMetadata().offset();

        // then
        await().atMost(15, TimeUnit.SECONDS).until(() -> positions().stream()
                .anyMatch(p -> p.partition() == expectedPartition && p.offset() == expectedOffset));
        ConsumedPosition position = positions().stream()
                .filter(p -> p.partition() == expectedPartition && p.offset() == expectedOffset)
                .findFirst().orElseThrow();
        assertEquals(KafkaSampleConsumer.TOPIC + "-" + expectedPartition + "-" + expectedOffset,
                position.messageId());
    }

    private List<String> messages() {
        String[] body = rest.getForObject("/messages", String[].class);
        return body == null ? List.of() : List.of(body);
    }

    private List<String> ids() {
        String[] ids = rest.getForObject("/messages/ids", String[].class);
        return ids == null ? List.of() : List.of(ids);
    }

    private List<ConsumedPosition> positions() {
        ConsumedPosition[] positions = rest.getForObject("/messages/positions", ConsumedPosition[].class);
        return positions == null ? List.of() : List.of(positions);
    }
}
