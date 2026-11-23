package cn.kennylee.banyan.samples.sign;

import cn.kennylee.banyan.openapi.sign.client.OpenApiRequestSigner;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * context-path 端到端测试：客户端用 basePath 剥离前缀后与服务端签名路径一致。
 *
 * @author kennylee
 * @since 0.1.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "server.servlet.context-path=/api")
class ContextPathEndToEndTest extends AbstractSignTest {

    /** basePath 剥离后验签通过。 */
    @Test
    void stripsContextPathBeforeSigning() {
        // given
        OpenApiRequestSigner signer = OpenApiRequestSigner.builder()
                .keyId("app1")
                .secret(SECRET)
                .basePath("/api")
                .build();
        RestTemplate rest = TestClients.restTemplate(signer);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // when
        ResponseEntity<String> response = rest.exchange(baseUrl() + "/api/openapi/orders",
                HttpMethod.POST, new HttpEntity<>("{\"id\":7}", headers), String.class);

        // then
        assertEquals(200, response.getStatusCode().value());
        assertEquals("{\"id\":7}", response.getBody());
    }

}
