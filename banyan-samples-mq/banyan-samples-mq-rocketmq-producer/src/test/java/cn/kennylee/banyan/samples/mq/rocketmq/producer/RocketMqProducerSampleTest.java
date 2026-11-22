package cn.kennylee.banyan.samples.mq.rocketmq.producer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RocketMQ 生产者示例测试：通过 HTTP 发送，验证接口返回。
 *
 * @author kennylee
 * @since 0.1.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
class RocketMqProducerSampleTest {

    @Container
    static final RocketMqContainers RMQ = new RocketMqContainers(MessageController.TOPIC);

    @DynamicPropertySource
    static void rocketMqProperties(DynamicPropertyRegistry registry) {
        registry.add("rocketmq.name-server", RMQ::nameServer);
    }

    @Autowired
    private TestRestTemplate rest;

    @Test
    void sendsPlainAndDelayedMessages() {
        // when / then
        assertTrue(post("/messages", "{\"id\":3}"));
        assertTrue(post("/messages/delay", "{\"id\":4}"));
    }

    @Test
    void sendsKeyedPayloadAndAfterCommitMessages() {
        // when / then
        assertTrue(post("/messages/keyed", "{\"id\":5}"));
        assertTrue(post("/messages/payload", "{\"id\":6}"));
        assertTrue(post("/messages/after-commit", "{\"id\":7}"));
    }

    private boolean post(String path, String body) {
        return rest.postForEntity(path, new MessageRequest(body), Void.class)
                .getStatusCode().is2xxSuccessful();
    }
}
