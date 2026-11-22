package cn.kennylee.banyan.samples.mq.multi;

import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * 多后端示例应用：同一个应用同时使用 Kafka 与 RocketMQ。
 *
 * @author kennylee
 * @since 0.1.0
 */
@SpringBootApplication
@Import(RocketMQAutoConfiguration.class)
public class MultiBackendSampleApplication {

    /**
     * 启动应用。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(MultiBackendSampleApplication.class, args);
    }
}
