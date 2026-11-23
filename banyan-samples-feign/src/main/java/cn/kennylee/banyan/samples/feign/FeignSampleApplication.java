package cn.kennylee.banyan.samples.feign;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Feign 能力样例应用：既是提供方，也是调用方。
 *
 * @author kennylee
 * @since 0.1.0
 */
@SpringBootApplication
@EnableFeignClients
public class FeignSampleApplication {

    /**
     * 启动应用。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(FeignSampleApplication.class, args);
    }
}
