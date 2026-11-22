package cn.kennylee.banyan.samples.mq.multi;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 记录各实例消费到的消息。
 *
 * @author kennylee
 * @since 0.1.0
 */
@Component
public class MessageStore {

    private final List<String> messages = new CopyOnWriteArrayList<>();

    /**
     * 追加一条记录。
     *
     * @param message 记录内容
     */
    public void add(String message) {
        messages.add(message);
    }

    /**
     * 全部记录。
     *
     * @return 只读快照
     */
    public List<String> all() {
        return List.copyOf(messages);
    }
}
