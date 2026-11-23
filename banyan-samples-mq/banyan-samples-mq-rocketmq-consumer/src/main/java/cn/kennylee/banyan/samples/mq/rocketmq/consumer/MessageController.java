package cn.kennylee.banyan.samples.mq.rocketmq.consumer;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 查询已消费消息。
 *
 * @author kennylee
 * @since 0.1.0
 */
@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageStore store;

    /**
     * 构造控制器。
     *
     * @param store 消息存储
     */
    public MessageController(MessageStore store) {
        this.store = store;
    }

    /**
     * 全部已消费消息。
     *
     * @return 消息体列表
     */
    @GetMapping
    public List<String> all() {
        return store.all();
    }

    /**
     * 全部已消费消息的 messageId，取自消费上下文。
     *
     * @return 消息 id 列表
     */
    @GetMapping("/ids")
    public List<String> ids() {
        return store.ids();
    }
}
