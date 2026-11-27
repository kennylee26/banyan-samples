package cn.kennylee.banyan.samples.excel;

import cn.kennylee.banyan.excel.exporter.BatchSource;
import cn.kennylee.banyan.excel.exporter.ExcelExportResult;
import cn.kennylee.banyan.excel.exporter.ExportCursor;
import cn.kennylee.banyan.excel.exporter.fastexcel.FastExcelExporters;
import cn.kennylee.banyan.samples.excel.model.OrderRow;
import cn.kennylee.banyan.samples.excel.support.SampleOrders;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 批次数据源契约示例：直接验证库按批次大小逐批取数。
 *
 * <p>这是库内部行为，不经过 HTTP；HTTP 用例见其他测试类。</p>
 *
 * @author kennylee
 * @since 0.1.0
 */
class BatchSourceSampleTest {

    @Test
    @DisplayName("库按批次大小逐批取数")
    void exportsInBatches() {
        // given
        List<OrderRow> data = SampleOrders.of(100);
        RecordingBatchSource source = new RecordingBatchSource(data);

        // when
        ExcelExportResult result = FastExcelExporters.xlsx(source, OrderRow.class)
            .batchSize(30)
            .export();

        // then
        try {
            assertEquals(100, result.totalRecords());
            assertEquals(List.of(30, 30, 30, 10), source.batchSizes);
        } finally {
            result.deleteFile();
        }
    }

    /**
     * 记录每批大小的数据源。
     */
    static final class RecordingBatchSource implements BatchSource<OrderRow> {

        private final List<OrderRow> data;

        final List<Integer> batchSizes = new ArrayList<>();

        RecordingBatchSource(List<OrderRow> data) {
            this.data = data;
        }

        @Override
        public List<OrderRow> loadNextBatch(ExportCursor cursor, int batchSize) {
            int from = cursor.exportedCount();
            if (from >= data.size()) {
                return List.of();
            }
            int to = Math.min(data.size(), from + batchSize);
            List<OrderRow> batch = new ArrayList<>(data.subList(from, to));
            batchSizes.add(batch.size());
            return batch;
        }
    }
}
