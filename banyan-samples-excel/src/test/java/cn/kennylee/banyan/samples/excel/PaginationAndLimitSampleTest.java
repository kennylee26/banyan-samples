package cn.kennylee.banyan.samples.excel;

import cn.kennylee.banyan.excel.exporter.ExcelExportResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 游标分页、上限截断与空数据样例。
 *
 * @author kennylee
 * @since 0.1.0
 */
class PaginationAndLimitSampleTest extends AbstractExcelSampleTest {

    @Test
    @DisplayName("游标分页导出全部数据且无重复")
    void exportsWithCursorPagination() {
        // given
        List<OrderRow> data = SampleOrders.of(100);

        // when
        ExcelExportResult result = exportService.exportWithCursor(data, 15);
        keep(result);

        // then
        assertEquals(100, result.totalRecords());
        assertEquals(data, ExcelTestSupport.read(result.file()));
        assertFalse(result.truncated());
    }

    @Test
    @DisplayName("超过最大条数时静默截断并标记")
    void truncatesAtLimit() {
        // given
        List<OrderRow> data = SampleOrders.of(100);

        // when
        ExcelExportResult result = exportService.exportWithLimit(data, 40);
        keep(result);

        // then
        assertEquals(40, result.totalRecords());
        assertTrue(result.truncated());
        assertEquals(40, ExcelTestSupport.read(result.file()).size());
    }

    @Test
    @DisplayName("空数据仍产出含 sheet 与表头的可读文件")
    void emptyDataProducesReadableFile() throws Exception {
        // when
        ExcelExportResult result = exportService.exportXlsx(List.of(), 20);
        keep(result);

        // then
        assertEquals(0, result.totalRecords());
        assertEquals(1, ExcelTestSupport.sheetCount(result.file()), "空数据也应建立 sheet");
        assertEquals("订单号", ExcelTestSupport.firstHeader(result.file()), "空数据也应写出表头");
        assertEquals(0, ExcelTestSupport.read(result.file()).size());
    }
}
