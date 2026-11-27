package cn.kennylee.banyan.samples.excel.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 五个上传解析接口的 MockMvc 用例。
 *
 * @author kennylee
 * @since 0.1.0
 */
class ParseEndpointsSampleTest extends AbstractParseSampleTest {

    @Test
    @DisplayName("POST /parse/xlsx 返回成功行")
    void parsesXlsx() throws Exception {
        mockMvc.perform(multipart("/parse/xlsx").file(xlsxFile(orders(3))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalRows").value(3))
            .andExpect(jsonPath("$.rows.length()").value(3))
            .andExpect(jsonPath("$.rows[0].orderNo").value("ORD-1"))
            .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    @DisplayName("POST /parse/xlsx 收集失败行并继续")
    void collectsFailedRows() throws Exception {
        mockMvc.perform(multipart("/parse/xlsx").file(badAmountFile()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rows.length()").value(1))
            .andExpect(jsonPath("$.errors.length()").value(1))
            .andExpect(jsonPath("$.errors[0].message").exists());
    }

    @Test
    @DisplayName("POST /parse/batch-save 只统计不保留批次")
    void batchSaves() throws Exception {
        mockMvc.perform(multipart("/parse/batch-save").file(xlsxFile(orders(5))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.savedRows").value(5))
            .andExpect(jsonPath("$.batchCount").value(1));
    }

    @Test
    @DisplayName("POST /parse/header-alias 映射星号表头")
    void mapsHeaderAlias() throws Exception {
        mockMvc.perform(multipart("/parse/header-alias").file(starHeaderFile("13800000001")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rows[0].mobile").value("13800000001"));
    }

    @Test
    @DisplayName("POST /parse/limit 触发截断")
    void limitsRows() throws Exception {
        mockMvc.perform(multipart("/parse/limit")
                .file(xlsxFile(orders(10)))
                .param("maxRows", "4"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalRows").value(4))
            .andExpect(jsonPath("$.truncated").value(true))
            .andExpect(jsonPath("$.rows.length()").value(4));
    }

    @Test
    @DisplayName("POST /parse/unordered 按行号还原文件顺序")
    void unorderedRestoresOrder() throws Exception {
        mockMvc.perform(multipart("/parse/unordered").file(xlsxFile(orders(100))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.batchCount").value(2))
            .andExpect(jsonPath("$.rows[0].orderNo").value("ORD-1"))
            .andExpect(jsonPath("$.rows[99].orderNo").value("ORD-100"));
    }
}
