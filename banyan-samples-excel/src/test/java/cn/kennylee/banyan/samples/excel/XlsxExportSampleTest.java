package cn.kennylee.banyan.samples.excel;

import cn.kennylee.banyan.samples.excel.support.SampleOrders;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.io.File;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * xlsx 接口样例：基础分页、模板与自动分 sheet。
 *
 * @author kennylee
 * @since 0.1.0
 */
class XlsxExportSampleTest extends AbstractExcelSampleTest {

    private static final MediaType XLSX = MediaType.parseMediaType(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    @Test
    @DisplayName("GET /export/xlsx 下载分页导出结果")
    void exportsXlsxWithPagination() throws Exception {
        // when
        byte[] body = mockMvc.perform(get("/export/xlsx").param("rows", "100"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("orders.xlsx")))
            .andExpect(content().contentType(XLSX))
            .andReturn().getResponse().getContentAsByteArray();

        // then
        File file = writeTemp(".xlsx", body);
        assertEquals(SampleOrders.of(100), ExcelTestSupport.read(file));
        file.delete();
    }

    @Test
    @DisplayName("GET /export/template 用模板导出且不重复表头")
    void exportsWithTemplate() throws Exception {
        // when
        byte[] body = mockMvc.perform(get("/export/template").param("rows", "50"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("orders-template.xlsx")))
            .andReturn().getResponse().getContentAsByteArray();

        // then
        File file = writeTemp(".xlsx", body);
        assertEquals(SampleOrders.of(50), ExcelTestSupport.read(file));
        file.delete();
    }

    @Test
    @DisplayName("GET /export/sheets 超过容量自动分 sheet")
    void splitsSheetsWhenCapacityExceeded() throws Exception {
        // when
        byte[] body = mockMvc.perform(get("/export/sheets")
                .param("rows", "100")
                .param("sheetCapacity", "30"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();

        // then
        File file = writeTemp(".xlsx", body);
        assertEquals(4, ExcelTestSupport.sheetCount(file), "100 行、容量 30 应分 4 个 sheet");
        assertEquals(SampleOrders.of(100), ExcelTestSupport.readAllSheets(file));
        file.delete();
    }
}
