package cn.kennylee.banyan.samples.excel;

import cn.kennylee.banyan.excel.exporter.BatchSource;
import cn.kennylee.banyan.excel.exporter.ExcelExportResult;
import cn.kennylee.banyan.excel.exporter.ExportCursor;
import cn.kennylee.banyan.excel.exporter.fastexcel.FastExcelExporters;
import cn.kennylee.banyan.samples.excel.model.OrderRow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 大数据量探针：类名不以 Test 结尾，默认不进常规套件。
 *
 * <p>手动执行：{@code mvn -pl banyan-samples-excel test -Dtest=LargeVolumeProbe}，
 * 打印耗时、文件大小与峰值内存，不做阈值断言。</p>
 *
 * @author kennylee
 * @since 0.1.0
 */
class LargeVolumeProbe {

    @Test
    @EnabledIfSystemProperty(named = "samples.excel.large", matches = "true")
    void probeFiveMillionRows() throws InterruptedException {
        int total = 5_000_000;
        AtomicLong peak = new AtomicLong(usedBytes());
        AtomicBoolean running = new AtomicBoolean(true);
        Thread sampler = new Thread(() -> {
            while (running.get()) {
                peak.accumulateAndGet(usedBytes(), Math::max);
                try {
                    Thread.sleep(20L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }, "memory-sampler");
        sampler.setDaemon(true);
        sampler.start();

        long start = System.currentTimeMillis();
        ExcelExportResult result = FastExcelExporters.xlsx(new GeneratedOrderSource(total), OrderRow.class)
            .batchSize(5_000)
            .export();
        long costMs = System.currentTimeMillis() - start;

        running.set(false);
        sampler.join(1_000L);

        System.out.println("[probe] rows=" + result.totalRecords()
            + ", costMs=" + costMs
            + ", fileMB=" + result.file().length() / 1024 / 1024
            + ", maxHeapMB=" + Runtime.getRuntime().maxMemory() / 1024 / 1024
            + ", peakUsedMB=" + peak.get() / 1024 / 1024);
        try {
            assertEquals(total, result.totalRecords());
            assertTrue(result.file().length() > 0);
        } finally {
            result.deleteFile();
        }
    }

    private static long usedBytes() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    /**
     * 按需生成、用完即弃的数据源，不持有全量数据。
     */
    static final class GeneratedOrderSource implements BatchSource<OrderRow> {

        private final int total;

        GeneratedOrderSource(int total) {
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
}
