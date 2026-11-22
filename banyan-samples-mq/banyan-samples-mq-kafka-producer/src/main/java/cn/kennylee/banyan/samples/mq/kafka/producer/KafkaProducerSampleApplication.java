package cn.kennylee.banyan.samples.mq.kafka.producer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Kafka 生产者示例应用。
 *
 * @author kennylee
 * @since 0.1.0
 */
@SpringBootApplication
public class KafkaProducerSampleApplication {

    /**
     * 启动应用。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(KafkaProducerSampleApplication.class, args);
    }
}
