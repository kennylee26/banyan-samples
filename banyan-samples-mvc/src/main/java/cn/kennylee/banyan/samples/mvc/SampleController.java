package cn.kennylee.banyan.samples.mvc;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

/**
 * 样例控制器：覆盖正常返回、请求参数错误与兜底异常。
 *
 * @author kennylee
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api")
public class SampleController {

    /** 兜底异常里故意携带的敏感信息，用于验证响应不回显。 */
    public static final String SECRET_DETAIL = "secret-detail";

    /**
     * 正常返回，验证成功响应不被包装；含一个固定日期用于验证 {@code spring.jackson.date-format}。
     *
     * @return 业务对象
     */
    @GetMapping("/ok")
    public Map<String, Object> ok() {
        return Map.of(
                "name", "banyan",
                "longValue", 42L,
                "date", new Date(0L),
                "localDateTime", LocalDateTime.of(2026, 11, 25, 10, 30, 0),
                "localDate", LocalDate.of(2026, 11, 25));
    }

    /**
     * 缺少必填参数，触发 {@code MissingServletRequestParameterException}。
     *
     * @param id 必填参数
     * @return 参数值
     */
    @GetMapping("/missing")
    public String missing(@RequestParam("id") String id) {
        return id;
    }

    /**
     * 参数类型不匹配，触发 {@code MethodArgumentTypeMismatchException}。
     *
     * @param id 整数参数
     * @return 参数值
     */
    @GetMapping("/type-mismatch")
    public String typeMismatch(@RequestParam("id") Integer id) {
        return String.valueOf(id);
    }

    /**
     * 抛出未预期异常，触发兜底 advice。
     *
     * @return 不会返回
     */
    @GetMapping("/boom")
    public String boom() {
        throw new IllegalStateException(SECRET_DETAIL);
    }
}
