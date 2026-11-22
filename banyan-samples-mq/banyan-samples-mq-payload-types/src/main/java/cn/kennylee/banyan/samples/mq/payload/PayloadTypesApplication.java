package cn.kennylee.banyan.samples.mq.payload;

import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * payload 类型示例应用。
 *
 * @author kennylee
 * @since 0.1.0
 */
@SpringBootApplication
@Import(RocketMQAutoConfiguration.class)
public class PayloadTypesApplication {

    /**
     * 启动应用。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(PayloadTypesApplication.class, args);
    }
}
