package cn.kennylee.banyan.samples.excel.source;

import cn.kennylee.banyan.excel.exporter.BatchSource;
import cn.kennylee.banyan.excel.exporter.ExportCursor;
import cn.kennylee.banyan.samples.excel.model.OrderRow;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于已导出行数的分页数据源，模拟按页码取数。
 *
 * <p>与 {@link OrderCursorSource} 刻意对比两种分页策略：两者都用「从/到 + subList」切片，
 * 差异只在定位下一批的起点（已导出行数 vs 上一批末条订单号）。</p>
 *
 * @author kennylee
 * @since 0.1.0
 */
public class OrderBatchSource implements BatchSource<OrderRow> {

    /** 全量数据。 */
    private final List<OrderRow> data;

    /**
     * 构造实例。
     *
     * @param data 全量数据
     */
    public OrderBatchSource(List<OrderRow> data) {
        this.data = data;
    }

    @Override
    public List<OrderRow> loadNextBatch(ExportCursor cursor, int batchSize) {
        int from = cursor.exportedCount();
        if (from >= data.size()) {
            return List.of();
        }
        int to = Math.min(data.size(), from + batchSize);
        return new ArrayList<>(data.subList(from, to));
    }
}
