package cn.kennylee.banyan.samples.mvc;

import cn.kennylee.banyan.spring.mvc.error.DefaultExceptionResultFactory;
import cn.kennylee.banyan.spring.mvc.error.ExceptionResultFactory;
import cn.kennylee.banyan.spring.mvc.handler.BanyanFallbackExceptionAdvice;
import cn.kennylee.banyan.spring.mvc.handler.BanyanWebExceptionAdvice;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 默认配置下的端到端测试：正常返回、请求参数错误、兜底异常。
 *
 * @author kennylee
 * @since 0.1.0
 */
@SpringBootTest
@AutoConfigureMockMvc
class MvcSampleEndToEndTest {

    /** 对外错误码协议值，故意写死以锁定公开契约。 */
    private static final String BAD_REQUEST_CODE = "banyan.spring.mvc.bad-request";

    /** 对外错误码协议值，故意写死以锁定公开契约。 */
    private static final String INTERNAL_ERROR_CODE = "banyan.spring.mvc.internal-error";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationContext context;

    /** 正常返回不被包装。 */
    @Test
    void normalResponseIsNotWrapped() throws Exception {
        // when
        ResultActions actions = mockMvc.perform(get("/api/ok"));

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("banyan"))
                .andExpect(jsonPath("$.longValue").value(42))
                .andExpect(jsonPath("$.code").doesNotExist())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    /** 缺少必填参数返回参数不合法错误，HTTP 200 且带 code/message。 */
    @Test
    void missingParameterReturnsBadRequestCode() throws Exception {
        // when
        ResultActions actions = mockMvc.perform(get("/api/missing"));

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(BAD_REQUEST_CODE))
                .andExpect(jsonPath("$.message").isString());
    }

    /** 参数类型不匹配返回参数不合法错误，HTTP 200 且带 code/message。 */
    @Test
    void typeMismatchReturnsBadRequestCode() throws Exception {
        // when
        ResultActions actions = mockMvc.perform(get("/api/type-mismatch").param("id", "abc"));

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(BAD_REQUEST_CODE))
                .andExpect(jsonPath("$.message").isString());
    }

    /** 兜底异常返回内部错误且不暴露堆栈。 */
    @Test
    void fallbackReturnsInternalErrorWithoutLeakingDetail() throws Exception {
        // when
        ResultActions actions = mockMvc.perform(get("/api/boom"));

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(INTERNAL_ERROR_CODE))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.message").value(not(containsString(SampleController.SECRET_DETAIL))))
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.stackTrace").doesNotExist());
    }

    /** 默认装配默认错误响应实现与异常 advice，且不注册成功响应包装。 */
    @Test
    void defaultBeansAreRegistered() {
        // when
        ExceptionResultFactory factory = context.getBean(ExceptionResultFactory.class);
        String[] bodyAdvices = context.getBeanNamesForType(ResponseBodyAdvice.class);

        // then
        assertThat(factory).isInstanceOf(DefaultExceptionResultFactory.class);
        assertThat(context.getBeanNamesForType(BanyanWebExceptionAdvice.class)).hasSize(1);
        assertThat(context.getBeanNamesForType(BanyanFallbackExceptionAdvice.class)).hasSize(1);
        assertThat(bodyAdvices).isEmpty();
    }
}
