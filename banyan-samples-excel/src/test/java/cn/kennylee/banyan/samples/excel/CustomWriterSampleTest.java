package cn.kennylee.banyan.samples.excel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 自定义写入器接口样例：纯 JDK 输出与失败场景。
 *
 * @author kennylee
 * @since 0.1.0
 */
class CustomWriterSampleTest extends AbstractExcelSampleTest {

    @Test
    @DisplayName("GET /export/custom 用纯 JDK 写入器导出")
    void exportsWithCustomWriter() throws Exception {
        // when
        byte[] body = mockMvc.perform(get("/export/custom").param("rows", "10"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("orders-custom.txt")))
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
            .andReturn().getResponse().getContentAsByteArray();

        // then
        File file = writeTemp(".txt", body);
        List<String> lines = Files.readAllLines(file.toPath());
        assertEquals(10, lines.size());
        assertEquals("ORD-00001;客户0;100;PAID", lines.get(0));
        file.delete();
    }

    @Test
    @DisplayName("GET /export/fail 写出失败返回 500")
    void returnsServerErrorWhenWriterFails() throws Exception {
        mockMvc.perform(get("/export/fail")).andExpect(status().isInternalServerError());
    }
}
