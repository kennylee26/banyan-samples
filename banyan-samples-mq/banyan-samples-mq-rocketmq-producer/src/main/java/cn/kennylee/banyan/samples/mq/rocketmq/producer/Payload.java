package cn.kennylee.banyan.samples.mq.rocketmq.producer;

/**
 * 仅用于演示自定义编解码的业务对象。
 *
 * @param value 业务数据
 * @author kennylee
 * @since 0.1.0
 */
public record Payload(String value) {
}
