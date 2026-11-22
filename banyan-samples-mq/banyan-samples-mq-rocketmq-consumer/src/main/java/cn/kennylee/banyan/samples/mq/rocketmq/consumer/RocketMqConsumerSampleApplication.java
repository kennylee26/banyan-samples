package cn.kennylee.banyan.samples.mq.rocketmq.consumer;

import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * RocketMQ 消费者示例应用。
 *
 * <p>starter 2.2.3 只提供 spring.factories，Boot 3 不读取，因此显式导入其自动配置。</p>
 *
 * @author kennylee
 * @since 0.1.0
 */
@SpringBootApplication
@Import(RocketMQAutoConfiguration.class)
public class RocketMqConsumerSampleApplication {

    /**
     * 启动应用。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(RocketMqConsumerSampleApplication.class, args);
    }
}
