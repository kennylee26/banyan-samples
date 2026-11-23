package cn.kennylee.banyan.samples.sign;

import cn.kennylee.banyan.openapi.sign.client.OpenApiRequestSigner;
import cn.kennylee.banyan.openapi.sign.client.httpclient5.SigningHttpRequests;
import cn.kennylee.banyan.openapi.sign.client.jdkhttp.SigningJdkHttpRequests;
import cn.kennylee.banyan.openapi.sign.client.resttemplate.SigningRestTemplates;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 加密模式端到端测试：两个客户端工具都能被提供方验签并解密。
 *
 * @author kennylee
 * @since 0.1.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SignClientEndToEndTest {

    /** 与 application.yml 一致的密钥。 */
    static final String SECRET = "secret-for-tests";

    @LocalServerPort
    private int port;

    /** HttpClient5：原地签名后用自己的 client 发送。 */
    @Test
    void httpClient5SendsEncryptedRequest() throws Exception {
        // given
        HttpPost post = new HttpPost(baseUrl() + "/openapi/orders");
        post.setEntity(new StringEntity("{\"id\":1}", ContentType.APPLICATION_JSON));
        SigningHttpRequests.sign(signer(), post);

        // when
        try (CloseableHttpClient client = HttpClientBuilder.create().build();
             CloseableHttpResponse response = client.execute(post)) {
            // then
            assertEquals(200, response.getCode());
            assertEquals("{\"id\":1}", EntityUtils.toString(response.getEntity(),
                    StandardCharsets.UTF_8));
        }
    }

    /** RestTemplate：给已有模板挂拦截器。 */
    @Test
    void restTemplateSendsEncryptedRequest() {
        // given
        RestTemplate rest = new RestTemplate();
        SigningRestTemplates.attach(rest, signer());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // when
        ResponseEntity<String> response = rest.exchange(baseUrl() + "/openapi/orders",
                HttpMethod.POST, new HttpEntity<>("{\"id\":2}", headers), String.class);

        // then
        assertEquals(200, response.getStatusCode().value());
        assertEquals("{\"id\":2}", response.getBody());
    }

    /** HttpClient5 能签名无 body 请求。 */
    @Test
    void httpClient5SignsEmptyBody() throws Exception {
        // given
        HttpGet get = new HttpGet(baseUrl() + "/openapi/orders/ping");
        SigningHttpRequests.sign(signer(), get);

        // when
        try (CloseableHttpClient client = HttpClientBuilder.create().build();
             CloseableHttpResponse response = client.execute(get)) {
            // then
            assertEquals(200, response.getCode());
            assertEquals("pong", EntityUtils.toString(response.getEntity(),
                    StandardCharsets.UTF_8));
        }
    }

    /** JDK HttpClient：传入请求 + body，拿到已签名请求。 */
    @Test
    void jdkHttpClientSendsEncryptedRequest() throws Exception {
        // given
        HttpRequest unsigned = HttpRequest.newBuilder(
                        URI.create(baseUrl() + "/openapi/orders"))
                .method("POST", HttpRequest.BodyPublishers.noBody())
                .header("Content-Type", "application/json")
                .build();

        // when
        HttpRequest signed = SigningJdkHttpRequests.sign(signer(), unsigned, "{\"id\":10}");
        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(signed, HttpResponse.BodyHandlers.ofString());

        // then
        assertEquals(200, response.statusCode());
        assertEquals("{\"id\":10}", response.body());
    }

    /** RestTemplate 能签名无 body 请求。 */
    @Test
    void restTemplateSignsEmptyBody() {
        // given
        RestTemplate rest = new RestTemplate();
        SigningRestTemplates.attach(rest, signer());

        // when
        ResponseEntity<String> response = rest.getForEntity(
                baseUrl() + "/openapi/orders/ping", String.class);

        // then
        assertEquals(200, response.getStatusCode().value());
        assertEquals("pong", response.getBody());
    }

    /** HttpClient5 使用错误密钥时被提供方拒绝。 */
    @Test
    void httpClient5RejectsWrongSecret() throws Exception {
        // given
        OpenApiRequestSigner wrong = OpenApiRequestSigner.builder()
                .keyId("app1")
                .secret("wrong-secret")
                .build();
        HttpPost post = new HttpPost(baseUrl() + "/openapi/orders");
        post.setEntity(new StringEntity("{\"id\":8}", ContentType.APPLICATION_JSON));
        SigningHttpRequests.sign(wrong, post);

        // when
        try (CloseableHttpClient client = HttpClientBuilder.create().build();
             CloseableHttpResponse response = client.execute(post)) {
            // then
            assertEquals(401, response.getCode());
        }
    }

    /** RestTemplate 使用错误密钥时被提供方拒绝。 */
    @Test
    void rejectsWrongSecret() {
        // given
        OpenApiRequestSigner wrong = OpenApiRequestSigner.builder()
                .keyId("app1")
                .secret("wrong-secret")
                .build();
        RestTemplate rest = new RestTemplate();
        SigningRestTemplates.attach(rest, wrong);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // when
        HttpClientErrorException error = assertThrows(HttpClientErrorException.class, () ->
                rest.exchange(baseUrl() + "/openapi/orders", HttpMethod.POST,
                        new HttpEntity<>("{\"id\":3}", headers), String.class));

        // then
        assertEquals(401, error.getStatusCode().value());
    }

    /** 构造签名门面。 */
    private OpenApiRequestSigner signer() {
        return OpenApiRequestSigner.builder().keyId("app1").secret(SECRET).build();
    }

    /** 服务根地址。 */
    private String baseUrl() {
        return "http://localhost:" + port;
    }
}
