package cn.kennylee.banyan.samples.excel;

import cn.kennylee.banyan.excel.exporter.ExcelExportResult;
import cn.kennylee.banyan.excel.exporter.fastexcel.FastExcelExporters;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CSV 样例：纯文本旁路导出，不按 sheet 容量拆分。
 *
 * @author kennylee
 * @since 0.1.0
 */
class CsvExportSampleTest extends AbstractExcelSampleTest {

    @Test
    @DisplayName("CSV 导出单文件，不按 sheet 容量拆分")
    void exportsCsv() {
        // given
        List<OrderRow> data = SampleOrders.of(100);

        // when
        ExcelExportResult result = FastExcelExporters.csv(new OrderBatchSource(data), OrderRow.class)
            .sheetCapacity(30)
            .export();
        keep(result);

        // then
        assertTrue(result.file().getName().endsWith(".csv"));
        assertEquals(100, result.totalRecords());
        assertEquals(data, ExcelTestSupport.read(result.file()), "CSV 不分 sheet，单文件包含全部行");
    }
}
