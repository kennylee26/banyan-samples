package cn.kennylee.banyan.samples.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

/**
 * 调用方 Feign client。
 *
 * @author kennylee
 * @since 0.1.0
 */
@FeignClient(name = "sample", url = "${sample.base-url:http://localhost:${SERVER_PORT:18768}}")
public interface SampleFeignClient {

    /**
     * 查询提供方收到的 header。
     *
     * @return header 摘要
     */
    @GetMapping("/sample/headers")
    Map<String, String> headers();

    /**
     * 回显请求体。
     *
     * @param body 请求体
     * @return 原文
     */
    @PostMapping("/sample/echo")
    String echo(@RequestBody String body);

    /**
     * 请求提供方回写指定 TraceId。
     *
     * @param traceId 期望响应头中的 TraceId
     * @return 固定响应
     */
    @GetMapping("/sample/trace")
    String trace(@RequestHeader("X-Desired-Trace") String traceId);
}
