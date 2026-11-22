package cn.kennylee.banyan.samples.mq.multi.config;

import cn.kennylee.banyan.mq.core.spi.MqBackend;
import cn.kennylee.banyan.mq.kafka.spring.KafkaMqBackend;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

/**
 * 两个 Kafka 集群：member 与 payment，各自一套 Template / ConsumerFactory 与后端。
 *
 * <p>同类型多实例无法只靠 {@code banyan.mq.instances.<name>.type} 区分，
 * 因此这里手动创建后端，并在 {@link MqInstanceRegistryConfig} 中显式映射实例名。</p>
 *
 * @author kennylee
 * @since 0.1.0
 */
@Configuration(proxyBeanMethods = false)
public class KafkaClustersConfig {

    /**
     * member 集群的发送模板。
     *
     * @param servers 集群地址
     * @return 模板
     */
    @Bean
    KafkaTemplate<String, String> memberKafkaTemplate(
            @Value("${samples.kafka.member.bootstrap-servers}") String servers) {
        return new KafkaTemplate<>(producerFactory(servers));
    }

    /**
     * member 集群的消费者工厂。
     *
     * @param servers 集群地址
     * @return 工厂
     */
    @Bean
    ConsumerFactory<String, String> memberKafkaConsumerFactory(
            @Value("${samples.kafka.member.bootstrap-servers}") String servers) {
        return consumerFactory(servers, "banyan-sample-member");
    }

    /**
     * payment 集群的发送模板。
     *
     * @param servers 集群地址
     * @return 模板
     */
    @Bean
    KafkaTemplate<String, String> paymentKafkaTemplate(
            @Value("${samples.kafka.payment.bootstrap-servers}") String servers) {
        return new KafkaTemplate<>(producerFactory(servers));
    }

    /**
     * payment 集群的消费者工厂。
     *
     * @param servers 集群地址
     * @return 工厂
     */
    @Bean
    ConsumerFactory<String, String> paymentKafkaConsumerFactory(
            @Value("${samples.kafka.payment.bootstrap-servers}") String servers) {
        return consumerFactory(servers, "banyan-sample-payment");
    }

    /**
     * member Kafka 后端。
     *
     * @param template 发送模板
     * @param factory  消费者工厂
     * @return 后端
     */
    @Bean
    MqBackend memberKafkaBackend(
            @Qualifier("memberKafkaTemplate") KafkaTemplate<String, String> template,
            @Qualifier("memberKafkaConsumerFactory") ConsumerFactory<String, String> factory) {
        return new KafkaMqBackend(template, factory);
    }

    /**
     * payment Kafka 后端。
     *
     * @param template 发送模板
     * @param factory  消费者工厂
     * @return 后端
     */
    @Bean
    MqBackend paymentKafkaBackend(
            @Qualifier("paymentKafkaTemplate") KafkaTemplate<String, String> template,
            @Qualifier("paymentKafkaConsumerFactory") ConsumerFactory<String, String> factory) {
        return new KafkaMqBackend(template, factory);
    }

    private static ProducerFactory<String, String> producerFactory(String servers) {
        Map<String, Object> props = Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    private static ConsumerFactory<String, String> consumerFactory(String servers, String group) {
        Map<String, Object> props = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers,
                ConsumerConfig.GROUP_ID_CONFIG, group,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }
}
