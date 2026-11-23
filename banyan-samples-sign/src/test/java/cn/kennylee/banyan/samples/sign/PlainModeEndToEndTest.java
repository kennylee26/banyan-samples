package cn.kennylee.banyan.samples.sign;

import cn.kennylee.banyan.openapi.sign.client.OpenApiRequestSigner;
import cn.kennylee.banyan.openapi.sign.client.httpclient5.SigningHttpRequests;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 明文模式端到端测试：提供方关闭加密时，客户端只签名原文。
 *
 * @author kennylee
 * @since 0.1.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "banyan.openapi-sign.encrypted=false")
class PlainModeEndToEndTest extends AbstractSignTest {

    /** HttpClient5 明文请求。 */
    @Test
    void httpClient5SendsPlainBody() throws Exception {
        // given
        HttpPost post = new HttpPost(baseUrl() + "/openapi/orders");
        post.setEntity(new StringEntity("{\"id\":4}", ContentType.APPLICATION_JSON));
        SigningHttpRequests.sign(signer(), post);

        // when
        try (CloseableHttpClient client = HttpClientBuilder.create().build();
             CloseableHttpResponse response = client.execute(post)) {
            // then
            assertEquals(200, response.getCode());
            assertEquals("{\"id\":4}", EntityUtils.toString(response.getEntity(),
                    StandardCharsets.UTF_8));
        }
    }

    /** RestTemplate 明文请求。 */
    @Test
    void restTemplateSendsPlainBody() {
        // given
        RestTemplate rest = TestClients.restTemplate(signer());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // when
        ResponseEntity<String> response = rest.exchange(baseUrl() + "/openapi/orders",
                HttpMethod.POST, new HttpEntity<>("{\"id\":5}", headers), String.class);

        // then
        assertEquals(200, response.getStatusCode().value());
        assertEquals("{\"id\":5}", response.getBody());
    }

    @Override
    protected OpenApiRequestSigner signer() {
        return OpenApiRequestSigner.builder().keyId("app1").secret(SECRET)
                .plainBody().build();
    }

}
