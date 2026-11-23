package cn.kennylee.banyan.samples.mq.rocketmq.consumer;

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

    private final List<String> messageIds = new CopyOnWriteArrayList<>();

    /**
     * 追加一条消息及其消费上下文的 messageId。
     *
     * @param messageId 消息 id
     * @param message   消息体
     */
    public void add(String messageId, String message) {
        messageIds.add(messageId);
        messages.add(message);
    }

    /**
     * 全部已消费消息的 messageId。
     *
     * @return 只读快照
     */
    public List<String> ids() {
        return List.copyOf(messageIds);
    }

    /**
     * 清空记录，供测试隔离。
     */
    public void clear() {
        messages.clear();
        messageIds.clear();
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
