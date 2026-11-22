package cn.kennylee.banyan.samples.mq.payload;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * 把 OrderKey 序列化为 JSON 对象的字符串 key。
 *
 * @author kennylee
 * @since 0.1.0
 */
public class OrderKeySerializer extends JsonSerializer<OrderKey> {

    @Override
    public void serialize(OrderKey key, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        gen.writeFieldName(key.id());
    }
}
