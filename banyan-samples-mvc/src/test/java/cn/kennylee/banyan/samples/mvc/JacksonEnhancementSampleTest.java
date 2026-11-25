package cn.kennylee.banyan.samples.mvc;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Jackson 增强样例：默认开启，Long 转字符串，Date 与 LocalDateTime 统一格式。
 *
 * @author kennylee
 * @since 0.1.0
 */
@SpringBootTest(properties = "spring.jackson.time-zone=UTC")
@AutoConfigureMockMvc
class JacksonEnhancementSampleTest {

    @Autowired
    private MockMvc mockMvc;

    /** Long 输出字符串；Date 与 LocalDateTime 都按模块默认格式输出。 */
    @Test
    void formatsLongAndDateTime() throws Exception {
        // when
        ResultActions actions = mockMvc.perform(get("/api/ok"));

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.longValue").value("42"))
                .andExpect(jsonPath("$.date").value("1970-01-01 00:00:00"))
                .andExpect(jsonPath("$.localDateTime").value("2026-11-25 10:30:00"))
                .andExpect(jsonPath("$.localDate").value("2026-11-25"));
    }
}
