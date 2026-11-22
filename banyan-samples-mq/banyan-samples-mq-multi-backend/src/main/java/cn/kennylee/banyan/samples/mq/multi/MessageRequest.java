package cn.kennylee.banyan.samples.mq.multi;

/**
 * 发送请求体。
 *
 * @param payload 业务数据
 * @author kennylee
 * @since 0.1.0
 */
public record MessageRequest(String payload) {
}
