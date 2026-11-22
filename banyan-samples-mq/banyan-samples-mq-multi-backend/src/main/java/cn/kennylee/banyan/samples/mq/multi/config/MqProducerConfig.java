package cn.kennylee.banyan.samples.mq.multi.config;

import cn.kennylee.banyan.mq.core.instance.MqInstanceRegistry;
import cn.kennylee.banyan.mq.producer.MqProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 三个生产者分别绑定一个逻辑实例。
 *
 * @author kennylee
 * @since 0.1.0
 */
@Configuration(proxyBeanMethods = false)
public class MqProducerConfig {

    /**
     * member Kafka 生产者。
     *
     * @param registry 逻辑实例路由
     * @return 生产者
     */
    @Bean
    MqProducer memberKafkaProducer(MqInstanceRegistry registry) {
        return new MqProducer(registry, "member-kafka");
    }

    /**
     * payment Kafka 生产者。
     *
     * @param registry 逻辑实例路由
     * @return 生产者
     */
    @Bean
    MqProducer paymentKafkaProducer(MqInstanceRegistry registry) {
        return new MqProducer(registry, "payment-kafka");
    }

    /**
     * RocketMQ 生产者。
     *
     * @param registry 逻辑实例路由
     * @return 生产者
     */
    @Bean
    MqProducer rocketmqProducer(MqInstanceRegistry registry) {
        return new MqProducer(registry, "member-rocketmq");
    }
}
