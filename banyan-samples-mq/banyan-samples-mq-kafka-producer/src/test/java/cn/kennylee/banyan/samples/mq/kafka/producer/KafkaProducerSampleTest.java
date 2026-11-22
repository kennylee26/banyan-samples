package cn.kennylee.banyan.samples.mq.kafka.producer;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Kafka 生产者示例测试：通过 HTTP 发送，再用原生消费者确认到达。
 *
 * @author kennylee
 * @since 0.1.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.kafka.consumer.auto-offset-reset=earliest")
@EmbeddedKafka(partitions = 1, topics = MessageController.TOPIC)
class KafkaProducerSampleTest {

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private ConsumerFactory<String, String> consumerFactory;

    @Test
    void sendsMessageThroughHttp() {
        // when
        ResponseEntity<Void> response = rest.postForEntity(
                "/messages", new MessageRequest("{\"id\":1}"), Void.class);

        // then
        assertTrue(response.getStatusCode().is2xxSuccessful(), "发送失败: " + response);
        assertEquals("{\"id\":1}", pollValue("{\"id\":1}").value());
    }

    @Test
    void sendsKeyAndHeaders() {
        // when
        rest.postForEntity("/messages/keyed", new MessageRequest("{\"id\":6}"), Void.class);

        // then
        ConsumerRecord<String, String> record = pollValue("{\"id\":6}");
        assertEquals("order-1", record.key());
        assertEquals("t1", new String(record.headers().lastHeader("tenant").value(),
                StandardCharsets.UTF_8));
    }

    @Test
    void encodesPayloadWithCustomCodec() {
        // when
        rest.postForEntity("/messages/payload", new MessageRequest("{\"id\":7}"), Void.class);

        // then
        assertEquals("encoded:{\"id\":7}", pollValue("encoded:{\"id\":7}").value());
    }

    private ConsumerRecord<String, String> pollValue(String expected) {
        try (Consumer<String, String> consumer =
                     consumerFactory.createConsumer("verify-" + UUID.randomUUID(), "verify")) {
            consumer.subscribe(List.of(MessageController.TOPIC));
            long deadline = System.currentTimeMillis() + 15000L;
            while (System.currentTimeMillis() < deadline) {
                for (ConsumerRecord<String, String> record : consumer.poll(Duration.ofSeconds(2))) {
                    if (expected.equals(record.value())) {
                        return record;
                    }
                }
            }
            fail("未收到消息: " + expected);
            return null;
        }
    }
}
