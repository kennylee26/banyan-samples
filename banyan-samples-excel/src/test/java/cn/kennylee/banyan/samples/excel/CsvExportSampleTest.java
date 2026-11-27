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
 * CSV 接口样例：单文件下载，不按 sheet 容量拆分。
 *
 * @author kennylee
 * @since 0.1.0
 */
class CsvExportSampleTest extends AbstractExcelSampleTest {

    @Test
    @DisplayName("GET /export/csv 下载单个 CSV 文件")
    void exportsCsv() throws Exception {
        // when
        byte[] body = mockMvc.perform(get("/export/csv").param("rows", "100"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("orders.csv")))
            .andReturn().getResponse().getContentAsByteArray();

        // then
        File file = writeTemp(".csv", body);
        assertEquals(SampleOrders.of(100), ExcelTestSupport.read(file), "CSV 单文件包含全部行");
    }
}
