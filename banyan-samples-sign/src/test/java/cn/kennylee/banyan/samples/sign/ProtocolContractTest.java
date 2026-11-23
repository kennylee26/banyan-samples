package cn.kennylee.banyan.samples.sign;

import cn.kennylee.banyan.commons.crypto.HmacSha256SignAlgorithm;
import cn.kennylee.banyan.commons.crypto.InMemoryNonceStore;
import cn.kennylee.banyan.commons.crypto.ReplayGuard;
import cn.kennylee.banyan.commons.crypto.SignKey;
import cn.kennylee.banyan.commons.crypto.SignRequest;
import cn.kennylee.banyan.commons.crypto.VerifyResult;
import cn.kennylee.banyan.openapi.sign.client.OpenApiRequestSigner;
import cn.kennylee.banyan.openapi.sign.client.SignHeaders;
import cn.kennylee.banyan.openapi.sign.client.SignedRequest;
import cn.kennylee.banyan.openapi.sign.client.UnsignedRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 跨模块协议契约：客户端内核签出的请求必须能被服务端 commons 验签。
 *
 * @author kennylee
 * @since 0.1.0
 */
class ProtocolContractTest {

    /** 测试密钥。 */
    private static final String SECRET = "secret-for-tests";

    /** 客户端签名结果通过服务端 ReplayGuard。 */
    @Test
    void clientSignaturePassesServerGuard() {
        // given
        OpenApiRequestSigner signer = OpenApiRequestSigner.builder()
                .keyId("app1").secret(SECRET).build();
        Map<String, List<String>> query = Map.of("page", List.of("1"));

        // when
        SignedRequest signed = signer.sign(new UnsignedRequest("POST", "/openapi/orders",
                query, "{\"id\":1}"));
        Map<String, String> headers = signed.headers();
        SignRequest request = new SignRequest("POST", "/openapi/orders", query, signed.body(),
                Long.parseLong(headers.get(SignHeaders.DEFAULT.timestamp())),
                headers.get(SignHeaders.DEFAULT.nonce()), "app1",
                headers.get(SignHeaders.DEFAULT.alg()), headers.get(SignHeaders.DEFAULT.sign()));
        ReplayGuard guard = new ReplayGuard(new HmacSha256SignAlgorithm(),
                new InMemoryNonceStore());

        // then
        assertEquals(VerifyResult.SUCCESS,
                guard.verify(request, Map.of("app1", new SignKey("app1", SECRET))));
    }

    /** 客户端默认 header 名与服务端一致。 */
    @Test
    void defaultHeaderNamesMatchServer() {
        // given
        SignHeaders client = SignHeaders.DEFAULT;
        cn.kennylee.banyan.commons.crypto.SignHeaders server =
                cn.kennylee.banyan.commons.crypto.SignHeaders.DEFAULT;

        // when / then
        assertEquals(server.alg(), client.alg());
        assertEquals(server.keyId(), client.keyId());
        assertEquals(server.timestamp(), client.timestamp());
        assertEquals(server.nonce(), client.nonce());
        assertEquals(server.sign(), client.sign());
    }
}
