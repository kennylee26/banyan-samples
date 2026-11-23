package cn.kennylee.banyan.samples.mq.kafka.consumer;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 业务服务示例：由消费者类注入，在消费方法里调用。
 *
 * @author kennylee
 * @since 0.1.0
 */
@Component
public class OrderService {

    private final List<String> handled = new CopyOnWriteArrayList<>();

    /**
     * 处理订单消息。
     *
     * @param body 消息体
     */
    public void handle(String body) {
        handled.add(body);
    }

    /**
     * 已处理的消息。
     *
     * @return 只读快照
     */
    public List<String> handled() {
        return List.copyOf(handled);
    }

    /**
     * 清空记录，供测试隔离。
     */
    public void clear() {
        handled.clear();
    }
}
