package cn.kennylee.banyan.samples.mq.kafka.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Kafka 消费者示例应用。
 *
 * @author kennylee
 * @since 0.1.0
 */
@SpringBootApplication
public class KafkaConsumerSampleApplication {

    /**
     * 启动应用。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(KafkaConsumerSampleApplication.class, args);
    }
}
