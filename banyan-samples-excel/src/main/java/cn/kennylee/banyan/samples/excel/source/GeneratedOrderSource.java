package cn.kennylee.banyan.samples.excel.source;

import cn.kennylee.banyan.excel.exporter.BatchSource;
import cn.kennylee.banyan.excel.exporter.ExportCursor;
import cn.kennylee.banyan.samples.excel.model.OrderRow;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 按需生成、用完即弃的数据源，用于大数据量演示，不持有全量数据。
 *
 * @author kennylee
 * @since 0.1.0
 */
public class GeneratedOrderSource implements BatchSource<OrderRow> {

    /** 总行数。 */
    private final int total;

    /**
     * 构造实例。
     *
     * @param total 总行数
     */
    public GeneratedOrderSource(int total) {
        this.total = total;
    }

    @Override
    public List<OrderRow> loadNextBatch(ExportCursor cursor, int batchSize) {
        int from = cursor.exportedCount();
        if (from >= total) {
            return List.of();
        }
        int to = Math.min(total, from + batchSize);
        List<OrderRow> rows = new ArrayList<>(to - from);
        for (int i = from; i < to; i++) {
            rows.add(new OrderRow(
                String.format(Locale.ROOT, "ORD-%08d", i + 1),
                "客户" + (i % 10),
                BigDecimal.valueOf(100L + i),
                i % 2 == 0 ? "PAID" : "PENDING"));
        }
        return rows;
    }
}
