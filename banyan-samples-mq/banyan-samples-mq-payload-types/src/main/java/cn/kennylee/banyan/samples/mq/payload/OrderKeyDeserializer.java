package cn.kennylee.banyan.samples.mq.payload;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;

/**
 * 把 JSON 对象的字符串 key 还原为 OrderKey。
 *
 * @author kennylee
 * @since 0.1.0
 */
public class OrderKeyDeserializer extends KeyDeserializer {

    @Override
    public Object deserializeKey(String key, DeserializationContext ctxt) {
        return new OrderKey(key);
    }
}
