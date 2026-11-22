package cn.kennylee.banyan.samples.mq.payload;

/**
 * 用作 Map key 的业务对象。
 *
 * <p>JSON 对象的 key 必须是字符串，因此需要配套的 KeySerializer / KeyDeserializer。</p>
 *
 * @param id 订单 id
 * @author kennylee
 * @since 0.1.0
 */
public record OrderKey(String id) {
}
