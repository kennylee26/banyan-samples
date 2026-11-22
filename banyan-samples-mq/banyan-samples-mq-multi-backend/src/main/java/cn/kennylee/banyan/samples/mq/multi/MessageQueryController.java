package cn.kennylee.banyan.samples.mq.multi;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 查询两个 Kafka 集群的消费结果。
 *
 * @author kennylee
 * @since 0.1.0
 */
@RestController
@RequestMapping("/messages")
public class MessageQueryController {

    private final MessageStore store;

    /**
     * 构造控制器。
     *
     * @param store 记录器
     */
    public MessageQueryController(MessageStore store) {
        this.store = store;
    }

    /**
     * 全部消费记录。
     *
     * @return 记录列表
     */
    @GetMapping
    public List<String> all() {
        return store.all();
    }
}
