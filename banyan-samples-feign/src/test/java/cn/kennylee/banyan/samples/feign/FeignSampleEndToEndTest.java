package cn.kennylee.banyan.samples.feign;

import cn.kennylee.banyan.commons.http.HeaderContextHolder;
import cn.kennylee.banyan.spring.feign.interceptor.ForwardHeadersInterceptor;
import cn.kennylee.banyan.trace.autoconfigure.TraceProperties;
import cn.kennylee.banyan.trace.context.TraceContext;
import cn.kennylee.banyan.trace.feign.FeignResponseTraceInterceptor;
import feign.Feign;
import feign.RequestTemplate;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Feign 能力端到端测试：真实 Feign client 调本机提供方。
 *
 * @author kennylee
 * @since 0.1.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class FeignSampleEndToEndTest {

    @Autowired
    private SampleFeignClient client;

    @Autowired
    private ObjectFactory<HttpMessageConverters> messageConverters;

    @Autowired
    private TraceProperties traceProperties;

    @AfterEach
    void clear() {
        RequestContextHolder.resetRequestAttributes();
        HeaderContextHolder.clear();
        org.slf4j.MDC.clear();
    }

    /** 默认不转发入站 header。 */
    @Test
    void doesNotForwardHeadersByDefault() {
        // given
        inbound("t1");

        // when
        Map<String, String> result = client.headers();

        // then
        assertEquals("null", result.get("X-Tenant"));
    }

    /** 响应日志解码器真实生效且不破坏原 Decoder。 */
    @Test
    void responseLoggingKeepsDecoderWorking() {
        // given
        Logger logger = (Logger) LoggerFactory.getLogger(
                cn.kennylee.banyan.spring.feign.logging.LoggingFeignDecoder.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            // when
            String result = client.echo("{\"id\":1}");

            // then
            assertEquals("{\"id\":1}", result);
            assertTrue(appender.list.stream()
                    .anyMatch(event -> event.getFormattedMessage().contains("Feign 响应")));
        } finally {
            logger.detachAppender(appender);
        }
    }

    /** trace 出向透传：MDC 里的 traceId 被写入 Feign 请求头。 */
    @Test
    void propagatesTraceId() {
        // given
        org.slf4j.MDC.put("traceId", "trace-1");

        // when
        Map<String, String> result = client.headers();

        // then
        assertEquals("trace-1", result.get("X-Trace-Id"));
    }

    /** 响应侧 traceId：调用链内临时补全，finally 后恢复为 null。 */
    @Test
    void fillsTraceIdFromResponseHeader() {
        // given
        org.junit.jupiter.api.Assertions.assertNull(TraceContext.getTraceId());
        SampleFeignClient tracingClient = Feign.builder()
                .contract(new SpringMvcContract())
                .decoder(new ResponseEntityDecoder(new SpringDecoder(messageConverters)))
                .responseInterceptor(new FeignResponseTraceInterceptor(traceProperties))
                .target(SampleFeignClient.class, baseUrl());

        // when
        String result = tracingClient.trace("provider-trace");

        // then
        assertEquals("ok", result);
        org.junit.jupiter.api.Assertions.assertNull(TraceContext.getTraceId());
    }

    /** 真实 HTTP 链路：开启转发后 header 到达提供方，敏感头被阻塞。 */
    @Test
    void forwardsHeadersThroughRealFeignCall() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Tenant", "t1");
        request.addHeader("Authorization", "Bearer secret");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        HeaderContextHolder.addCustomHeader("X-Tenant", "override");
        SampleFeignClient forwardingClient = Feign.builder()
                .contract(new SpringMvcContract())
                .decoder(new ResponseEntityDecoder(new SpringDecoder(messageConverters)))
                .requestInterceptor(new ForwardHeadersInterceptor(properties()))
                .target(SampleFeignClient.class, baseUrl());

        // when
        Map<String, String> result = forwardingClient.headers();

        // then
        assertEquals("override", result.get("X-Tenant"));
        assertEquals("null", result.get("Authorization"));
    }

    /** 单次禁用转发后不写 header。 */
    @Test
    void disablesForwardingPerCall() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Tenant", "t1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        HeaderContextHolder.disablePropagation();
        RequestTemplate template = new RequestTemplate();

        // when
        new ForwardHeadersInterceptor(properties()).apply(template);

        // then
        assertTrue(template.headers().isEmpty());
    }

    /** 单次上下文：阻塞头大小写无关、自定义头覆盖、apply 后自动清理。 */
    @Test
    void appliesPerCallOverrides() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("x-tenant", "origin");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        HeaderContextHolder.addBlockedHeader("X-TENANT");
        HeaderContextHolder.addCustomHeader("X-Tenant", "override");
        RequestTemplate template = new RequestTemplate();

        // when
        new ForwardHeadersInterceptor(properties()).apply(template);

        // then
        assertEquals("override", template.headers().get("X-Tenant").iterator().next());
        assertTrue(HeaderContextHolder.current().getCustomHeaders().isEmpty());
    }

    /** 关闭脱敏后日志打印敏感 header 原值。 */
    @Test
    void printsSensitiveHeaderWhenMaskingDisabled() {
        // given
        cn.kennylee.banyan.spring.feign.autoconfigure.FeignProperties props = properties();
        props.getLogging().setMaskSensitive(false);
        Map<String, String> headers = Map.of("Authorization", "Bearer secret");

        // when
        String text = cn.kennylee.banyan.commons.log.LogSanitizer
                .headers(headers, props.getLogging());

        // then
        assertTrue(text.contains("Bearer secret"));
    }

    /** 总开关关闭时 Feign 能力相关的上下文仍可正常使用。 */
    @Test
    void contextHolderWorksIndependentlyOfGlobalSwitch() {
        // given
        org.junit.jupiter.api.Assertions.assertTrue(HeaderContextHolder.isPropagationEnabled());

        // when
        HeaderContextHolder.setGloballyEnabled(false);
        boolean disabled = HeaderContextHolder.isPropagationEnabled();
        HeaderContextHolder.setGloballyEnabled(true);

        // then
        org.junit.jupiter.api.Assertions.assertFalse(disabled);
        org.junit.jupiter.api.Assertions.assertTrue(HeaderContextHolder.isPropagationEnabled());
    }

    /** 转发拦截器使用的默认配置。 */
    private static cn.kennylee.banyan.spring.feign.autoconfigure.FeignProperties properties() {
        return new cn.kennylee.banyan.spring.feign.autoconfigure.FeignProperties();
    }

    /** 服务根地址；与 application.yml 的默认端口一致。 */
    private String baseUrl() {
        return "http://localhost:18768";
    }

    /** 设置入站请求上下文。 */
    private void inbound(String tenant) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Tenant", tenant);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }
}
