package cn.kennylee.banyan.samples.excel.parser.handler;

import cn.kennylee.banyan.excel.parser.BatchHandler;
import cn.kennylee.banyan.excel.parser.ReadBatch;
import cn.kennylee.banyan.samples.excel.parser.model.OrderRow;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 模拟批量存库的处理器：每批只累加计数，不保留批次数据。
 *
 * <p>真实业务在这里调用自己的批量写库方法；库不负责事务与存储。</p>
 *
 * @author kennylee
 * @since 0.1.0
 */
public class InMemorySaveHandler implements BatchHandler<OrderRow> {

    private final AtomicInteger savedRows = new AtomicInteger();

    private final AtomicInteger batchCount = new AtomicInteger();

    @Override
    public void handle(ReadBatch<OrderRow> batch) {
        savedRows.addAndGet(batch.size());
        batchCount.incrementAndGet();
    }

    /**
     * 已保存行数。
     *
     * @return 行数
     */
    public int savedRows() {
        return savedRows.get();
    }

    /**
     * 已处理批次数。
     *
     * @return 批次数
     */
    public int batchCount() {
        return batchCount.get();
    }
}
