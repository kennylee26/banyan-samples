package cn.kennylee.banyan.samples.mq.kafka.consumer;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 记录已消费的消息，供 HTTP 查询。
 *
 * @author kennylee
 * @since 0.1.0
 */
@Component
public class MessageStore {

    private final List<String> messages = new CopyOnWriteArrayList<>();

    private final List<ConsumedPosition> positions = new CopyOnWriteArrayList<>();

    /**
     * 追加一条消息及其定位信息。
     *
     * @param message  消息体
     * @param position 定位信息
     */
    public void add(String message, ConsumedPosition position) {
        messages.add(message);
        positions.add(position);
    }

    /**
     * 全部已消费消息的 messageId。
     *
     * @return 只读快照
     */
    public List<String> ids() {
        return positions.stream().map(ConsumedPosition::messageId).toList();
    }

    /**
     * 全部已消费消息的定位信息。
     *
     * @return 只读快照
     */
    public List<ConsumedPosition> positions() {
        return List.copyOf(positions);
    }

    /**
     * 清空记录，供测试隔离。
     */
    public void clear() {
        messages.clear();
        positions.clear();
    }

    /**
     * 全部已消费消息。
     *
     * @return 只读快照
     */
    public List<String> all() {
        return List.copyOf(messages);
    }
}
