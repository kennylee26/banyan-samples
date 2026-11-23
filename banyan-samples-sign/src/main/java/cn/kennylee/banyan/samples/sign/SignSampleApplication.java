package cn.kennylee.banyan.samples.sign;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * OpenAPI 签名样例应用：提供方与客户端工具共用一个进程。
 *
 * @author kennylee
 * @since 0.1.0
 */
@SpringBootApplication
public class SignSampleApplication {

    /**
     * 启动应用。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SignSampleApplication.class, args);
    }
}
