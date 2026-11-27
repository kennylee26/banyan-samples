package cn.kennylee.banyan.samples.excel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 大数据量接口样例：小规模触达 /export/large 的接线，验证行数响应头与导出结果。
 *
 * @author kennylee
 * @since 0.1.0
 */
class LargeEndpointSampleTest extends AbstractExcelSampleTest {

    @Test
    @DisplayName("GET /export/large 按行数响应头返回完整数据")
    void exportsLargeEndpoint() throws Exception {
        // when
        byte[] body = mockMvc.perform(get("/export/large").param("rows", "30000"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Export-Rows", "30000"))
            .andReturn().getResponse().getContentAsByteArray();

        // then
        File file = writeTemp(".xlsx", body);
        assertEquals(30000, ExcelTestSupport.readAllSheets(file).size());
    }
}
