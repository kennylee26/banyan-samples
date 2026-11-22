package cn.kennylee.banyan.samples.mq.multi;

import cn.kennylee.banyan.mq.consumer.autoconfigure.MqConsumerAutoConfiguration;
import cn.kennylee.banyan.mq.core.MqCapability;
import cn.kennylee.banyan.mq.core.MqMessage;
import cn.kennylee.banyan.mq.core.instance.MqInstanceRegistry;
import cn.kennylee.banyan.mq.core.spi.MqBackend;
import cn.kennylee.banyan.mq.core.spi.MqConsumerDefinition;
import cn.kennylee.banyan.mq.core.spi.MqConsumerHandler;
import cn.kennylee.banyan.mq.core.spi.MqSendRequest;
import cn.kennylee.banyan.mq.spring.MqCommonsAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 多 Kafka 消费者测试：不连真实 broker，只验证按实例名分别注册。
 *
 * @author kennylee
 * @since 0.1.0
 */
class MultiKafkaConsumerTest {

    private final RecordingBackend memberKafka = new RecordingBackend("kafka");
    private final RecordingBackend paymentKafka = new RecordingBackend("kafka");

    @Test
    void registersConsumersToDifferentInstances() {
        // when / then
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        MqCommonsAutoConfiguration.class, MqConsumerAutoConfiguration.class))
                .withBean(MqInstanceRegistry.class, () -> new MqInstanceRegistry(Map.of(
                        "member-kafka", memberKafka,
                        "payment-kafka", paymentKafka)))
                .withUserConfiguration(ConsumerConfig.class)
                .run(context -> {
                    assertThat(memberKafka.definitions()).hasSize(1);
                    assertThat(paymentKafka.definitions()).hasSize(1);
                    assertThat(memberKafka.definitions().get(0).getTopic())
                            .isEqualTo("banyan-sample-member-kafka");
                    assertThat(paymentKafka.definitions().get(0).getTopic())
                            .isEqualTo("banyan-sample-payment-kafka");
                });
    }

    /**
     * 消费者配置。
     *
     * @author kennylee
     * @since 0.1.0
     */
    @Configuration(proxyBeanMethods = false)
    static class ConsumerConfig {

        @Bean
        MemberKafkaConsumer memberKafkaConsumer() {
            return new MemberKafkaConsumer(new MessageStore());
        }

        @Bean
        PaymentKafkaConsumer paymentKafkaConsumer() {
            return new PaymentKafkaConsumer(new MessageStore());
        }
    }

    /**
     * 记录消费者定义的假后端。
     *
     * @author kennylee
     * @since 0.1.0
     */
    static class RecordingBackend implements MqBackend {

        private final String type;

        private final List<MqConsumerDefinition> definitions = new ArrayList<>();

        RecordingBackend(String type) {
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
            definitions.add(definition);
        }

        List<MqConsumerDefinition> definitions() {
            return definitions;
        }
    }
}
