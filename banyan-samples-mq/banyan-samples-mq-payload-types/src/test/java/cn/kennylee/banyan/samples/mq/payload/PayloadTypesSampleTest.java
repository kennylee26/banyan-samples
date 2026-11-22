package cn.kennylee.banyan.samples.mq.payload;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * payload 类型矩阵示例测试：每一种类型走一遍真实收发与转换。
 *
 * @author kennylee
 * @since 0.1.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
class PayloadTypesSampleTest {

    @Container
    static final RocketMqContainers RMQ = new RocketMqContainers(PayloadConsumer.TOPIC);

    @DynamicPropertySource
    static void rocketMqProperties(DynamicPropertyRegistry registry) {
        registry.add("rocketmq.name-server", RMQ::nameServer);
    }

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private MessageStore store;

    @Test
    void supportsAllPayloadTypes() {
        // when
        rest.postForEntity("/messages/string", new StringPayload("hello"), Void.class);
        rest.postForEntity("/messages/integer", new IntPayload(123), Void.class);
        rest.postForEntity("/messages/boolean", new BoolPayload(true), Void.class);
        rest.postForEntity("/messages/pojo", new Order("o1", "n1"), Void.class);
        rest.postForEntity("/messages/list", List.of(new Order("o2", "n2")), Void.class);
        rest.postForEntity("/messages/map", Map.of("o3", new Order("o3", "n3")), Void.class);
        rest.postForEntity("/messages/map-bean",
                Map.of("o4", List.of(new Order("o4", "n4"))), Void.class);

        // then
        await().atMost(40, TimeUnit.SECONDS).until(() -> store.all().size() >= 7);
        assertThat(store.all()).contains(
                "string:hello",
                "int:123",
                "boolean:true",
                "pojo:o1",
                "list:1",
                "map:1",
                "map-bean:1");
    }
}
