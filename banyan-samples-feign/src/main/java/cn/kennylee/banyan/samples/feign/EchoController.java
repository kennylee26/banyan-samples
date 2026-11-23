package cn.kennylee.banyan.samples.feign;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 提供方：回显请求头与 body，用于验证 header 转发与响应解码。
 *
 * @author kennylee
 * @since 0.1.0
 */
@RestController
@RequestMapping("/sample")
public class EchoController {

    /**
     * 回显入站 header。
     *
     * @param request HTTP 请求
     * @return header 摘要
     */
    @GetMapping("/headers")
    public Map<String, String> headers(HttpServletRequest request) {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("X-Tenant", String.valueOf(request.getHeader("X-Tenant")));
        result.put("X-Trace-Id", String.valueOf(request.getHeader("X-Trace-Id")));
        result.put("Authorization", String.valueOf(request.getHeader("Authorization")));
        return result;
    }

    /**
     * 回显请求体，验证响应日志解码器没有破坏原 Decoder。
     *
     * @param body 请求体
     * @return 原文
     */
    @PostMapping("/echo")
    public String echo(@RequestBody String body) {
        return body;
    }

    /**
     * 按请求头回写 TraceId，用于演示响应侧补全。
     *
     * @param traceId 期望回写的 TraceId
     * @param response HTTP 响应
     * @return 固定响应
     */
    @GetMapping("/trace")
    public String trace(@org.springframework.web.bind.annotation.RequestHeader(
            value = "X-Desired-Trace", required = false) String traceId,
                        HttpServletResponse response) {
        if (traceId != null) {
            response.setHeader("X-Trace-Id", traceId);
        }
        return "ok";
    }
}
