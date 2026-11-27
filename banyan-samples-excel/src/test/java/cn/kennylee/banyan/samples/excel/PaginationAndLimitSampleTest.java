package cn.kennylee.banyan.samples.excel;

import cn.kennylee.banyan.samples.excel.support.SampleOrders;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 游标分页、上限截断与空数据接口样例。
 *
 * @author kennylee
 * @since 0.1.0
 */
class PaginationAndLimitSampleTest extends AbstractExcelSampleTest {

    @Test
    @DisplayName("GET /export/cursor 游标分页导出全部数据")
    void exportsWithCursorPagination() throws Exception {
        // when
        byte[] body = mockMvc.perform(get("/export/cursor").param("rows", "100"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("orders-cursor.xlsx")))
            .andReturn().getResponse().getContentAsByteArray();

        // then
        File file = writeTemp(".xlsx", body);
        assertEquals(SampleOrders.of(100), ExcelTestSupport.read(file));
        file.delete();
    }

    @Test
    @DisplayName("GET /export/limit 触顶截断并用响应头标记")
    void truncatesAtLimit() throws Exception {
        // when
        byte[] body = mockMvc.perform(get("/export/limit")
                .param("rows", "100")
                .param("maxRecords", "40"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Export-Truncated", "true"))
            .andReturn().getResponse().getContentAsByteArray();

        // then
        File file = writeTemp(".xlsx", body);
        assertEquals(40, ExcelTestSupport.read(file).size());
        file.delete();
    }

    @Test
    @DisplayName("GET /export/xlsx?rows=0 空数据仍产出含 sheet 与表头的文件")
    void emptyDataProducesReadableFile() throws Exception {
        // when
        byte[] body = mockMvc.perform(get("/export/xlsx").param("rows", "0"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();

        // then
        File file = writeTemp(".xlsx", body);
        assertEquals(1, ExcelTestSupport.sheetCount(file), "空数据也应建立 sheet");
        assertEquals("订单号", ExcelTestSupport.firstHeader(file), "空数据也应写出表头");
        assertEquals(0, ExcelTestSupport.read(file).size());
        file.delete();
    }
}
