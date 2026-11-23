package cn.kennylee.banyan.samples.sign;

import cn.kennylee.banyan.openapi.sign.client.OpenApiRequestSigner;
import cn.kennylee.banyan.openapi.sign.client.resttemplate.SigningRestTemplates;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpClient;

/**
 * 测试用客户端工厂：RestTemplate 显式禁止跟随重定向，再挂签名拦截器。
 *
 * @author kennylee
 * @since 0.1.0
 */
final class TestClients {

    private TestClients() {
    }

    /**
     * 创建禁止重定向并已签名的 RestTemplate。
     *
     * @param signer 签名门面
     * @return RestTemplate
     */
    static RestTemplate restTemplate(OpenApiRequestSigner signer) {
        HttpClient httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
        RestTemplate restTemplate = new RestTemplate(
                new JdkClientHttpRequestFactory(httpClient));
        return SigningRestTemplates.attach(restTemplate, signer);
    }
}
