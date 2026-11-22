package cn.kennylee.banyan.samples.mq.kafka.producer.config;

import cn.kennylee.banyan.mq.core.MqMessageCodec;
import cn.kennylee.banyan.samples.mq.kafka.producer.Payload;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 演示如何替换默认编解码器：payload 发送时由这里决定 body 内容。
 *
 * @author kennylee
 * @since 0.1.0
 */
@Configuration(proxyBeanMethods = false)
public class CodecConfig {

    /**
     * 自定义编解码器，覆盖默认的 String 直通实现。
     *
     * @return 编解码器
     */
    @Bean
    MqMessageCodec sampleCodec() {
        return new MqMessageCodec() {
            @Override
            public String encode(Object payload) {
                if (payload instanceof Payload sample) {
                    return "encoded:" + sample.value();
                }
                return payload == null ? null : String.valueOf(payload);
            }

            @Override
            public Object decode(String body, Class<?> type) {
                return body;
            }
        };
    }
}
