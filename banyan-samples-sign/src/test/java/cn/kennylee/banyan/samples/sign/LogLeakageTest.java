package cn.kennylee.banyan.samples.sign;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import cn.kennylee.banyan.openapi.sign.client.OpenApiRequestSigner;
import cn.kennylee.banyan.openapi.sign.client.resttemplate.SigningRestTemplates;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 日志脱敏测试：TRACE 全开时也不输出 secret、明文 body 与完整签名。
 *
 * @author kennylee
 * @since 0.1.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LogLeakageTest extends AbstractSignTest {

    /** 明文 body，用于断言不会出现在日志里。 */
    private static final String PLAINTEXT = "{\"id\":9}";

    /** TRACE 日志不泄漏敏感内容。 */
    @Test
    void traceLogsDoNotLeakSensitiveValues() {
        // given
        Logger logger = (Logger) LoggerFactory.getLogger(OpenApiRequestSigner.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        Level previous = logger.getLevel();
        logger.setLevel(Level.TRACE);
        logger.addAppender(appender);
        try {
            // when
            RestTemplate rest = SigningRestTemplates.create(OpenApiRequestSigner.builder()
                    .keyId("app1").secret(SECRET).build());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            rest.exchange(baseUrl() + "/openapi/orders", HttpMethod.POST,
                    new HttpEntity<>(PLAINTEXT, headers), String.class);
        } finally {
            logger.detachAppender(appender);
            logger.setLevel(previous);
        }

        // then
        String logs = appender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .collect(Collectors.joining("\n"));
        assertTrue(logs.contains("OpenAPI 规范化串"));
        assertFalse(logs.contains(SECRET));
        assertFalse(logs.contains(PLAINTEXT));
        assertTrue(logs.matches("(?s).*X-Banyan-Sign=[0-9a-f]{8}\\.\\.\\..*"));
    }

}
