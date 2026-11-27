package cn.kennylee.banyan.samples.excel.parser.controller;

import cn.kennylee.banyan.samples.excel.parser.model.BatchSaveResponse;
import cn.kennylee.banyan.samples.excel.parser.model.ParseResponse;
import cn.kennylee.banyan.samples.excel.parser.service.OrderParseService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * Excel 上传解析接口。
 *
 * @author kennylee
 * @since 0.1.0
 */
@RestController
@RequestMapping("/parse")
public class OrderParseController {

    private final OrderParseService service;

    /**
     * 构造实例。
     *
     * @param service 解析服务
     */
    public OrderParseController(OrderParseService service) {
        this.service = service;
    }

    /**
     * 流式读取上传文件，返回成功行与失败行。
     *
     * @param file 上传文件
     * @return 解析响应
     * @throws IOException 读取上传流失败
     */
    @PostMapping("/xlsx")
    public ParseResponse xlsx(@RequestParam("file") MultipartFile file) throws IOException {
        try (InputStream source = file.getInputStream()) {
            return service.parseXlsx(source);
        }
    }

    /**
     * 模拟批量存库。
     *
     * @param file 上传文件
     * @return 批量存库响应
     * @throws IOException 读取上传流失败
     */
    @PostMapping("/batch-save")
    public BatchSaveResponse batchSave(@RequestParam("file") MultipartFile file) throws IOException {
        try (InputStream source = file.getInputStream()) {
            return service.batchSave(source);
        }
    }

    /**
     * 用表头别名读取。
     *
     * @param file 上传文件
     * @return 解析响应
     * @throws IOException 读取上传流失败
     */
    @PostMapping("/header-alias")
    public ParseResponse headerAlias(@RequestParam("file") MultipartFile file) throws IOException {
        try (InputStream source = file.getInputStream()) {
            return service.parseWithHeaderAlias(source);
        }
    }

    /**
     * 按最大行数截断读取。
     *
     * @param file    上传文件
     * @param maxRows 最大行数
     * @return 解析响应
     * @throws IOException 读取上传流失败
     */
    @PostMapping("/limit")
    public ParseResponse limit(@RequestParam("file") MultipartFile file,
                               @RequestParam(value = "maxRows", defaultValue = "4") int maxRows) throws IOException {
        try (InputStream source = file.getInputStream()) {
            return service.parseWithLimit(source, maxRows);
        }
    }

    /**
     * 关闭保序、并行处理后按行号还原。
     *
     * @param file 上传文件
     * @return 解析响应
     * @throws IOException 读取上传流失败
     */
    @PostMapping("/unordered")
    public ParseResponse unordered(@RequestParam("file") MultipartFile file) throws IOException {
        try (InputStream source = file.getInputStream()) {
            return service.parseUnordered(source);
        }
    }
}
