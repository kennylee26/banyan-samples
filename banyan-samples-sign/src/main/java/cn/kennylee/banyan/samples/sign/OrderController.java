package cn.kennylee.banyan.samples.sign;

import cn.kennylee.banyan.openapi.sign.annotation.OpenApiSigned;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 受签名保护的示例接口：验签与解密由 starter 完成，这里只看到明文。
 *
 * @author kennylee
 * @since 0.1.0
 */
@RestController
@RequestMapping("/openapi/orders")
@OpenApiSigned
public class OrderController {

    /**
     * 回显明文请求体。
     *
     * @param body 明文 JSON
     * @return 原文
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public String create(@RequestBody String body) {
        return body;
    }

    /**
     * 无 body 的探测接口。
     *
     * @return 固定响应
     */
    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}
