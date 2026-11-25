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
 * 可选 Jackson 增强样例：Long 转字符串，日期格式复用 Spring Boot 原生键。
 *
 * @author kennylee
 * @since 0.1.0
 */
@SpringBootTest(properties = {
        "banyan.spring.mvc.jackson.enabled=true",
        "spring.jackson.date-format=yyyy-MM-dd HH:mm:ss",
        "spring.jackson.time-zone=UTC"})
@AutoConfigureMockMvc
class JacksonEnhancementSampleTest {

    @Autowired
    private MockMvc mockMvc;

    /** 开启增强后 Long 输出字符串；日期格式由 spring.jackson.date-format 生效。 */
    @Test
    void longAsStringAndNativeDateFormat() throws Exception {
        // when
        ResultActions actions = mockMvc.perform(get("/api/ok"));

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.longValue").value("42"))
                .andExpect(jsonPath("$.date").value("1970-01-01 00:00:00"));
    }
}
