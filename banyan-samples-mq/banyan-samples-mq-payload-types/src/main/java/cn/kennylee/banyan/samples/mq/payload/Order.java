package cn.kennylee.banyan.samples.mq.payload;

/**
 * 业务对象示例。
 *
 * @param id   订单 id
 * @param name 订单名称
 * @author kennylee
 * @since 0.1.0
 */
public record Order(String id, String name) {
}
