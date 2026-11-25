package cn.kennylee.banyan.samples.mvc;

import cn.kennylee.banyan.spring.mvc.error.DefaultExceptionResultFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 自定义错误响应样例：使用方实现替换默认结构。
 *
 * @author kennylee
 * @since 0.1.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("custom-factory")
class CustomFactorySampleTest {

    /** 对外错误码协议值，故意写死以锁定公开契约。 */
    private static final String INTERNAL_ERROR_CODE = "banyan.spring.mvc.internal-error";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationContext context;

    /** 响应体形状由使用方实现决定，默认实现让位。 */
    @Test
    void customFactoryChangesResponseShape() throws Exception {
        // when
        ResultActions actions = mockMvc.perform(get("/api/boom"));
        String[] defaultFactories = context.getBeanNamesForType(DefaultExceptionResultFactory.class);

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(INTERNAL_ERROR_CODE))
                .andExpect(jsonPath("$.detail").value(not(containsString(SampleController.SECRET_DETAIL))))
                .andExpect(jsonPath("$.message").doesNotExist());
        assertThat(defaultFactories).isEmpty();
    }
}
