package cn.kennylee.banyan.samples.mvc;

import cn.kennylee.banyan.spring.mvc.error.ExceptionResultFactory;
import cn.kennylee.banyan.spring.mvc.handler.BanyanFallbackExceptionAdvice;
import cn.kennylee.banyan.spring.mvc.handler.BanyanFeignExceptionAdvice;
import cn.kennylee.banyan.spring.mvc.handler.BanyanWebExceptionAdvice;
import cn.kennylee.banyan.spring.mvc.jackson.BanyanJacksonCustomizer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * 总开关关闭样例：本模块 bean 全部不注册，异常回到应用原有处理。
 *
 * @author kennylee
 * @since 0.1.0
 */
@SpringBootTest(properties = {
        "banyan.spring.mvc.enabled=false",
        "banyan.spring.mvc.jackson.enabled=true"})
@AutoConfigureMockMvc
class DisabledSampleTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    /** 关闭总开关后不注册任何本模块 bean（含已开启的 Jackson 增强）。 */
    @Test
    void noBanyanMvcBeansWhenDisabled() {
        // when
        String[] factories = context.getBeanNamesForType(ExceptionResultFactory.class);
        String[] webAdvices = context.getBeanNamesForType(BanyanWebExceptionAdvice.class);
        String[] fallbackAdvices = context.getBeanNamesForType(BanyanFallbackExceptionAdvice.class);
        String[] feignAdvices = context.getBeanNamesForType(BanyanFeignExceptionAdvice.class);
        String[] jacksonCustomizers = context.getBeanNamesForType(BanyanJacksonCustomizer.class);

        // then
        assertThat(factories).isEmpty();
        assertThat(webAdvices).isEmpty();
        assertThat(fallbackAdvices).isEmpty();
        assertThat(feignAdvices).isEmpty();
        assertThat(jacksonCustomizers).isEmpty();
    }

    /** 关闭总开关后异常不再被本模块转换为错误响应。 */
    @Test
    void exceptionFallsBackToContainerHandling() {
        // when / then
        assertThatThrownBy(() -> mockMvc.perform(get("/api/boom")))
                .isInstanceOf(jakarta.servlet.ServletException.class);
    }
}
