package cn.kennylee.banyan.samples.excel;

import cn.kennylee.banyan.excel.exporter.BatchSource;
import cn.kennylee.banyan.excel.exporter.ExcelExportResult;
import cn.kennylee.banyan.excel.exporter.ExportCursor;
import cn.kennylee.banyan.excel.exporter.fastexcel.FastExcelExporters;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * xlsx 样例：分页批次、模板填充与自动分 sheet。
 *
 * @author kennylee
 * @since 0.1.0
 */
class XlsxExportSampleTest extends AbstractExcelSampleTest {

    @Test
    @DisplayName("按批次大小逐批取数并导出 xlsx")
    void exportsXlsxWithPagination() {
        // given
        List<OrderRow> data = SampleOrders.of(100);
        RecordingBatchSource source = new RecordingBatchSource(data);

        // when
        ExcelExportResult result = FastExcelExporters.xlsx(source, OrderRow.class)
            .batchSize(30)
            .export();
        keep(result);

        // then
        assertEquals(100, result.totalRecords());
        assertEquals(List.of(30, 30, 30, 10), source.batchSizes, "库应按批次大小逐批取数");
        assertEquals(data, ExcelTestSupport.read(result.file()));
        assertFalse(result.truncated());
    }

    @Test
    @DisplayName("带模板导出不重复表头")
    void exportsWithTemplate() throws Exception {
        // given
        File template = ExcelTestSupport.templateWithHeader();
        List<OrderRow> data = SampleOrders.of(50);

        // when
        ExcelExportResult result;
        try (InputStream templateStream = Files.newInputStream(template.toPath())) {
            result = exportService.exportWithTemplate(data, templateStream);
        }
        keep(result);

        // then
        assertEquals(data, ExcelTestSupport.read(result.file()));
        assertFalse(result.truncated());
        template.delete();
    }

    @Test
    @DisplayName("超过 sheet 容量时自动分 sheet 且数据完整")
    void splitsSheetsWhenCapacityExceeded() throws Exception {
        // given
        List<OrderRow> data = SampleOrders.of(100);

        // when
        ExcelExportResult result = exportService.exportWithSheetCapacity(data, 30);
        keep(result);

        // then
        assertEquals(100, result.totalRecords());
        assertEquals(4, ExcelTestSupport.sheetCount(result.file()), "100 行、容量 30 应分 4 个 sheet");
        assertEquals(data, ExcelTestSupport.readAllSheets(result.file()));
    }

    /**
     * 记录每批大小的数据源，用于验证库确实逐批取数。
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
