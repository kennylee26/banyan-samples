package cn.kennylee.banyan.samples.mq.multi;

import cn.kennylee.banyan.mq.core.MqCapability;
import cn.kennylee.banyan.mq.core.MqMessage;
import cn.kennylee.banyan.mq.core.instance.MqInstanceRegistry;
import cn.kennylee.banyan.mq.core.spi.MqBackend;
import cn.kennylee.banyan.mq.core.spi.MqConsumerDefinition;
import cn.kennylee.banyan.mq.core.spi.MqConsumerHandler;
import cn.kennylee.banyan.mq.core.spi.MqSendRequest;
import cn.kennylee.banyan.mq.producer.autoconfigure.MqProducerAutoConfiguration;
import cn.kennylee.banyan.mq.spring.MqCommonsAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 多后端示例测试：不连真实 broker，只验证实例映射与路由。
 *
 * @author kennylee
 * @since 0.1.0
 */
class MultiBackendRoutingTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    MqCommonsAutoConfiguration.class, MqProducerAutoConfiguration.class))
            .withBean("kafkaBackend", MqBackend.class, () -> new FakeBackend("kafka"))
            .withBean("rocketmqBackend", MqBackend.class, () -> new FakeBackend("rocketmq"));

    @Test
    void registersTwoLogicalInstances() {
        // when / then
        runner.withPropertyValues(
                        "banyan.mq.instances.member-kafka.type=kafka",
                        "banyan.mq.instances.member-rocketmq.type=rocketmq")
                .run(context -> {
                    MqInstanceRegistry registry = context.getBean(MqInstanceRegistry.class);
                    assertThat(registry.instanceNames())
                            .containsExactlyInAnyOrder("member-kafka", "member-rocketmq");
                    assertThat(registry.backendOf("member-kafka").type()).isEqualTo("kafka");
                    assertThat(registry.backendOf("member-rocketmq").type()).isEqualTo("rocketmq");
                });
    }

    /**
     * 测试用后端。
     *
     * @author kennylee
     * @since 0.1.0
     */
    static class FakeBackend implements MqBackend {

        private final String type;

        FakeBackend(String type) {
            this.type = type;
        }

        @Override
        public String type() {
            return type;
        }

        @Override
        public Set<MqCapability> capabilities() {
            return Set.of();
        }

        @Override
        public void send(MqSendRequest request, MqMessage message) {
        }

        @Override
        public void registerConsumer(MqConsumerDefinition definition, MqConsumerHandler handler) {
        }
    }
}
