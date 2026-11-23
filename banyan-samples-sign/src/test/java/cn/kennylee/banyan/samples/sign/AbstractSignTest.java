package cn.kennylee.banyan.samples.sign;

import cn.kennylee.banyan.openapi.sign.client.OpenApiRequestSigner;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * 样例测试基类：统一密钥、端口与签名门面。
 *
 * @author kennylee
 * @since 0.1.0
 */
abstract class AbstractSignTest {

    /** 与 application.yml 一致的密钥。 */
    static final String SECRET = "secret-for-tests";

    @LocalServerPort
    protected int port;

    /**
     * 服务根地址。
     *
     * @return 地址
     */
    protected String baseUrl() {
        return "http://localhost:" + port;
    }

    /**
     * 默认加密模式签名门面。
     *
     * @return 签名门面
     */
    protected OpenApiRequestSigner signer() {
        return OpenApiRequestSigner.builder().keyId("app1").secret(SECRET).build();
    }
}
