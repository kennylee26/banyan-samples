package cn.kennylee.banyan.samples.mvc;

import cn.kennylee.banyan.spring.mvc.error.ExceptionResultFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * 自定义错误响应样例：在 {@code custom-factory} profile 下替换默认结构。
 *
 * @author kennylee
 * @since 0.1.0
 */
@Configuration(proxyBeanMethods = false)
@Profile("custom-factory")
public class CustomResultConfiguration {

    /**
     * 注册自定义错误响应实现。
     *
     * @return 自定义错误响应实现
     */
    @Bean
    ExceptionResultFactory customExceptionResultFactory() {
        return context -> new SampleResult(context.code(), context.message());
    }

    /**
     * 样例自定义响应结构。
     *
     * @param code   错误码
     * @param detail 错误消息
     * @author kennylee
     * @since 0.1.0
     */
    public record SampleResult(String code, String detail) {
    }
}
