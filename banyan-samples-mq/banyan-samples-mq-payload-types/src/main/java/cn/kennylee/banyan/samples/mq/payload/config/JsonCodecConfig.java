package cn.kennylee.banyan.samples.mq.payload.config;

import cn.kennylee.banyan.commons.spring.json.JacksonJsonCodec;
import cn.kennylee.banyan.commons.spring.json.JsonCodec;
import cn.kennylee.banyan.samples.mq.payload.OrderKey;
import cn.kennylee.banyan.samples.mq.payload.OrderKeyDeserializer;
import cn.kennylee.banyan.samples.mq.payload.OrderKeySerializer;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 自定义 JsonCodec：注册 OrderKey 的 key 序列化，使 Map&lt;OrderKey, List&lt;Order&gt;&gt; 可以转 JSON。
 *
 * @author kennylee
 * @since 0.1.0
 */
@Configuration(proxyBeanMethods = false)
public class JsonCodecConfig {

    /**
     * 带 OrderKey key 序列化的 JsonCodec。
     *
     * @return JsonCodec
     */
    @Bean
    JsonCodec payloadJsonCodec() {
        SimpleModule module = new SimpleModule()
                .addKeySerializer(OrderKey.class, new OrderKeySerializer())
                .addKeyDeserializer(OrderKey.class, new OrderKeyDeserializer());
        ObjectMapper mapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .addModule(module)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
        return new JacksonJsonCodec(mapper);
    }
}
