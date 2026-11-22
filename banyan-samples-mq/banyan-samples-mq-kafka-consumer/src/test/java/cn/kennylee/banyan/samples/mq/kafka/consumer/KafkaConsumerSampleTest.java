package cn.kennylee.banyan.samples.mq.kafka.consumer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

    @Test
    void consumesWithMqConsumer() {
        // when
        template.send(KafkaSampleConsumer.TOPIC, "{\"id\":2}");

        // then
        await().atMost(15, TimeUnit.SECONDS).until(() -> !messages().isEmpty());
        assertEquals(List.of("{\"id\":2}"), messages());
        assertEquals(List.of("{\"id\":2}"), orderService.handled());
    }

    private List<String> messages() {
        String[] body = rest.getForObject("/messages", String[].class);
        return body == null ? List.of() : List.of(body);
    }
}
