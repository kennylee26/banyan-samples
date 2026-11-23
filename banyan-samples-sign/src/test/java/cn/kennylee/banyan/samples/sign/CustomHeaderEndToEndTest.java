package cn.kennylee.banyan.samples.sign;

import cn.kennylee.banyan.openapi.sign.client.OpenApiRequestSigner;
import cn.kennylee.banyan.openapi.sign.client.SignHeaders;
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
 * 自定义 header 名端到端测试：客户端与提供方必须使用同一组名字。
 *
 * @author kennylee
 * @since 0.1.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "banyan.openapi-sign.headers.alg=X-Acme-Alg",
        "banyan.openapi-sign.headers.key-id=X-Acme-Key",
        "banyan.openapi-sign.headers.timestamp=X-Acme-Time",
        "banyan.openapi-sign.headers.nonce=X-Acme-Nonce",
        "banyan.openapi-sign.headers.sign=X-Acme-Sign"
})
class CustomHeaderEndToEndTest extends AbstractSignTest {

    /** 与提供方一致的自定义 header 名。 */
    private static final SignHeaders HEADERS = new SignHeaders(
            "X-Acme-Alg", "X-Acme-Key", "X-Acme-Time", "X-Acme-Nonce", "X-Acme-Sign");

    /** 自定义 header 名仍能通过验签。 */
    @Test
    void signsWithCustomHeaderNames() {
        // given
        OpenApiRequestSigner signer = OpenApiRequestSigner.builder()
                .keyId("app1")
                .secret(SECRET)
                .headers(HEADERS)
                .build();
        RestTemplate rest = TestClients.restTemplate(signer);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // when
        ResponseEntity<String> response = rest.exchange(baseUrl() + "/openapi/orders",
                HttpMethod.POST, new HttpEntity<>("{\"id\":6}", headers), String.class);

        // then
        assertEquals(200, response.getStatusCode().value());
        assertEquals("{\"id\":6}", response.getBody());
    }

}
