package cn.kennylee.banyan.samples.excel.source;

import cn.kennylee.banyan.excel.exporter.BatchSource;
import cn.kennylee.banyan.excel.exporter.ExportCursor;
import cn.kennylee.banyan.samples.excel.model.OrderRow;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于上一批末条订单号的游标（keyset）数据源。
 *
 * <p>数据须按订单号升序排列；每批写完后由 {@link #lastIdOf(List)} 回传末条订单号。
 * 与 {@link OrderBatchSource} 刻意对比两种分页策略：切片逻辑相同，差异只在定位下一批的起点。</p>
 *
 * @author kennylee
 * @since 0.1.0
 */
public class OrderCursorSource implements BatchSource<OrderRow> {

    /** 已按订单号升序排好的数据。 */
    private final List<OrderRow> data;

    /**
     * 构造实例。
     *
     * @param data 已按订单号升序排好的数据
     */
    public OrderCursorSource(List<OrderRow> data) {
        this.data = data;
    }

    @Override
    public List<OrderRow> loadNextBatch(ExportCursor cursor, int batchSize) {
        int from = indexAfter(cursor.prevPageLastId());
        if (from >= data.size()) {
            return List.of();
        }
        int to = Math.min(data.size(), from + batchSize);
        return new ArrayList<>(data.subList(from, to));
    }

    @Override
    public String lastIdOf(List<OrderRow> rows) {
        return rows.isEmpty() ? null : rows.get(rows.size() - 1).getOrderNo();
    }

    private int indexAfter(String lastId) {
        if (lastId == null) {
            return 0;
        }
        for (int i = 0; i < data.size(); i++) {
            if (lastId.equals(data.get(i).getOrderNo())) {
                return i + 1;
            }
        }
        return data.size();
    }
}
