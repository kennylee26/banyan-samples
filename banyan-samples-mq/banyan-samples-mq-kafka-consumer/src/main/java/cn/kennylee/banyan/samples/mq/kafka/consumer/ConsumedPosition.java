package cn.kennylee.banyan.samples.mq.kafka.consumer;

/**
 * Kafka 消费位置：分区号与分区内 offset。
 *
 * @param messageId 框架合成的定位串，格式为 topic-partition-offset
 * @param partition 分区号
 * @param offset    分区内 offset
 * @author kennylee
 * @since 0.1.0
 */
public record ConsumedPosition(String messageId, int partition, long offset) {
}
