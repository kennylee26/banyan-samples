package cn.kennylee.banyan.samples.excel.controller;

import cn.kennylee.banyan.excel.exporter.ExcelExportResult;
import cn.kennylee.banyan.samples.excel.service.OrderExportService;
import cn.kennylee.banyan.samples.excel.support.SampleOrders;
import cn.kennylee.banyan.samples.excel.support.SampleTemplates;
import cn.kennylee.banyan.samples.excel.writer.FailingOrderWriter;
import cn.kennylee.banyan.samples.excel.writer.SemicolonOrderWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * 导出接口样例：每个接口对应 {@code excel-exporter} 的一项能力，同步导出后直接下载文件。
 *
 * @author kennylee
 * @since 0.1.0
 */
@RestController
@RequestMapping("/export")
public class OrderExportController {

    /** xlsx 媒体类型。 */
    private static final MediaType XLSX = MediaType.parseMediaType(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    /** 导出服务。 */
    private final OrderExportService exportService;

    public OrderExportController(OrderExportService exportService) {
        this.exportService = exportService;
    }

    /**
     * 基础分页批次导出。
     *
     * @param rows     导出条数
     * @param response HTTP 响应
     * @throws IOException 写出失败
     */
    @GetMapping("/xlsx")
    public void xlsx(@RequestParam(defaultValue = "100") int rows,
                     HttpServletResponse response) throws IOException {
        ExcelExportResult result = exportService.exportXlsx(SampleOrders.of(rows), 30);
        download(result, "orders.xlsx", XLSX, response);
    }

    /**
     * 带模板导出。
     *
     * @param rows     导出条数
     * @param response HTTP 响应
     * @throws IOException 写出失败
     */
    @GetMapping("/template")
    public void template(@RequestParam(defaultValue = "50") int rows,
                         HttpServletResponse response) throws IOException {
        try (InputStream template = SampleTemplates.headerOnly("订单号", "客户", "金额", "状态")) {
            ExcelExportResult result = exportService.exportWithTemplate(SampleOrders.of(rows), template);
            download(result, "orders-template.xlsx", XLSX, response);
        }
    }

    /**
     * 自动分 sheet 导出。
     *
     * @param rows          导出条数
     * @param sheetCapacity 单 sheet 容量
     * @param response      HTTP 响应
     * @throws IOException 写出失败
     */
    @GetMapping("/sheets")
    public void sheets(@RequestParam(defaultValue = "100") int rows,
                       @RequestParam(defaultValue = "30") int sheetCapacity,
                       HttpServletResponse response) throws IOException {
        ExcelExportResult result = exportService.exportWithSheetCapacity(SampleOrders.of(rows), sheetCapacity);
        download(result, "orders-sheets.xlsx", XLSX, response);
    }

    /**
     * CSV 导出。
     *
     * @param rows     导出条数
     * @param response HTTP 响应
     * @throws IOException 写出失败
     */
    @GetMapping("/csv")
    public void csv(@RequestParam(defaultValue = "100") int rows,
                    HttpServletResponse response) throws IOException {
        ExcelExportResult result = exportService.exportCsv(SampleOrders.of(rows));
        download(result, "orders.csv", MediaType.parseMediaType("text/csv"), response);
    }

    /**
     * 游标分页导出。
     *
     * @param rows     导出条数
     * @param response HTTP 响应
     * @throws IOException 写出失败
     */
    @GetMapping("/cursor")
    public void cursor(@RequestParam(defaultValue = "100") int rows,
                       HttpServletResponse response) throws IOException {
        ExcelExportResult result = exportService.exportWithCursor(SampleOrders.of(rows), 15);
        download(result, "orders-cursor.xlsx", XLSX, response);
    }

    /**
     * 上限截断导出，响应头 {@code X-Export-Truncated} 标记是否触顶。
     *
     * @param rows       导出条数
     * @param maxRecords 最大条数
     * @param response   HTTP 响应
     * @throws IOException 写出失败
     */
    @GetMapping("/limit")
    public void limit(@RequestParam(defaultValue = "100") int rows,
                      @RequestParam(defaultValue = "40") int maxRecords,
                      HttpServletResponse response) throws IOException {
        ExcelExportResult result = exportService.exportWithLimit(SampleOrders.of(rows), maxRecords);
        response.setHeader("X-Export-Truncated", String.valueOf(result.truncated()));
        download(result, "orders-limit.xlsx", XLSX, response);
    }

    /**
     * 自定义纯 JDK 写入器导出（分号分隔文本）。
     *
     * @param rows     导出条数
     * @param response HTTP 响应
     * @throws IOException 写出失败
     */
    @GetMapping("/custom")
    public void custom(@RequestParam(defaultValue = "10") int rows,
                       HttpServletResponse response) throws IOException {
        ExcelExportResult result = exportService.exportWithCustomWriter(
            SampleOrders.of(rows), new SemicolonOrderWriter());
        download(result, "orders-custom.txt", MediaType.TEXT_PLAIN, response);
    }

    /**
     * 大数据量导出：按需生成数据，自动分 sheet。
     *
     * <p>默认 500 万行约 25 秒、约 100MB；仅用于演示，请按需调小 {@code rows}。</p>
     *
     * @param rows     导出条数
     * @param response HTTP 响应
     * @throws IOException 写出失败
     */
    @GetMapping("/large")
    public void large(@RequestParam(defaultValue = "5000000") int rows,
                      HttpServletResponse response) throws IOException {
        ExcelExportResult result = exportService.exportLarge(rows);
        response.setHeader("X-Export-Rows", String.valueOf(result.totalRecords()));
        download(result, "orders-large.xlsx", XLSX, response);
    }

    /**
     * 失败场景：写出器抛异常，返回 500。
     *
     * @param response HTTP 响应
     */
    @GetMapping("/fail")
    public void fail(HttpServletResponse response) {
        try {
            exportService.exportWithCustomWriter(SampleOrders.of(5), new FailingOrderWriter());
        } catch (RuntimeException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private static void download(ExcelExportResult result, String fileName, MediaType mediaType,
                                 HttpServletResponse response) throws IOException {
        try {
            response.setContentType(mediaType.toString());
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            response.setContentLengthLong(result.file().length());
            Files.copy(result.file().toPath(), response.getOutputStream());
        } finally {
            result.deleteFile();
        }
    }
}
