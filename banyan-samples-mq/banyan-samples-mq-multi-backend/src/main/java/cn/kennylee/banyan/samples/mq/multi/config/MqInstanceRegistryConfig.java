package cn.kennylee.banyan.samples.mq.multi.config;

import cn.kennylee.banyan.mq.core.instance.MqInstanceRegistry;
import cn.kennylee.banyan.mq.core.spi.MqBackend;
import cn.kennylee.banyan.mq.rocketmq.apache.RocketMqBackend;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * 显式声明实例名到后端的映射：两个 Kafka 集群 + 一个 RocketMQ。
 *
 * @author kennylee
 * @since 0.1.0
 */
@Configuration(proxyBeanMethods = false)
public class MqInstanceRegistryConfig {

    /**
     * 逻辑实例路由。
     *
     * @param memberKafka   member Kafka 后端
     * @param paymentKafka  payment Kafka 后端
     * @param rocketMq      RocketMQ 后端
     * @return 路由
     */
    @Bean
    MqInstanceRegistry mqInstanceRegistry(
            @Qualifier("memberKafkaBackend") MqBackend memberKafka,
            @Qualifier("paymentKafkaBackend") MqBackend paymentKafka,
            RocketMqBackend rocketMq) {
        return new MqInstanceRegistry(Map.of(
                "member-kafka", memberKafka,
                "payment-kafka", paymentKafka,
                "member-rocketmq", rocketMq));
    }
}
