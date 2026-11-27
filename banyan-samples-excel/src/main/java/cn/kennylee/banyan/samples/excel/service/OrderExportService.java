package cn.kennylee.banyan.samples.excel.service;

import cn.kennylee.banyan.excel.exporter.ExcelExportResult;
import cn.kennylee.banyan.excel.exporter.ExcelExporterBuilder;
import cn.kennylee.banyan.excel.exporter.ExcelRowWriter;
import cn.kennylee.banyan.excel.exporter.fastexcel.FastExcelExporters;
import cn.kennylee.banyan.samples.excel.model.OrderRow;
import cn.kennylee.banyan.samples.excel.source.GeneratedOrderSource;
import cn.kennylee.banyan.samples.excel.source.OrderBatchSource;
import cn.kennylee.banyan.samples.excel.source.OrderCursorSource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

/**
 * 导出样例服务：把不同导出场景收敛成可读的方法，供接口与演示调用。
 *
 * @author kennylee
 * @since 0.1.0
 */
@Service
public class OrderExportService {

    /** 大数据量导出批次大小。 */
    private static final int LARGE_BATCH_SIZE = 5_000;

    /**
     * 基础 xlsx 导出。
     *
     * @param rows      数据
     * @param batchSize 批次大小
     * @return 导出结果
     */
    public ExcelExportResult exportXlsx(List<OrderRow> rows, int batchSize) {
        return FastExcelExporters.xlsx(new OrderBatchSource(rows), OrderRow.class)
            .batchSize(batchSize)
            .export();
    }

    /**
     * 带模板的 xlsx 导出。
     *
     * @param rows     数据
     * @param template 模板输入流
     * @return 导出结果
     */
    public ExcelExportResult exportWithTemplate(List<OrderRow> rows, InputStream template) {
        return FastExcelExporters.xlsx(new OrderBatchSource(rows), OrderRow.class)
            .template(template)
            .export();
    }

    /**
     * 指定 sheet 容量的 xlsx 导出，用于触发自动分 sheet。
     *
     * @param rows          数据
     * @param sheetCapacity 单 sheet 容量
     * @return 导出结果
     */
    public ExcelExportResult exportWithSheetCapacity(List<OrderRow> rows, int sheetCapacity) {
        return FastExcelExporters.xlsx(new OrderBatchSource(rows), OrderRow.class)
            .sheetCapacity(sheetCapacity)
            .export();
    }

    /**
     * CSV 导出。
     *
     * @param rows 数据
     * @return 导出结果
     */
    public ExcelExportResult exportCsv(List<OrderRow> rows) {
        return FastExcelExporters.csv(new OrderBatchSource(rows), OrderRow.class).export();
    }

    /**
     * 游标（keyset）分页导出。
     *
     * @param rows      已按订单号升序排好的数据
     * @param batchSize 批次大小
     * @return 导出结果
     */
    public ExcelExportResult exportWithCursor(List<OrderRow> rows, int batchSize) {
        return FastExcelExporters.xlsx(new OrderCursorSource(rows), OrderRow.class)
            .batchSize(batchSize)
            .export();
    }

    /**
     * 指定最大条数的 xlsx 导出。
     *
     * @param rows       数据
     * @param maxRecords 最大条数
     * @return 导出结果
     */
    public ExcelExportResult exportWithLimit(List<OrderRow> rows, int maxRecords) {
        return FastExcelExporters.xlsx(new OrderBatchSource(rows), OrderRow.class)
            .maxExportRecords(maxRecords)
            .export();
    }

    /**
     * 大数据量导出：按需生成数据，不驻留全量集合。
     *
     * @param rows 总行数
     * @return 导出结果
     */
    public ExcelExportResult exportLarge(int rows) {
        return FastExcelExporters.xlsx(new GeneratedOrderSource(rows), OrderRow.class)
            .batchSize(LARGE_BATCH_SIZE)
            .export();
    }

    /**
     * 使用自定义行写入器导出，验证公共契约可脱离 FastExcel 使用。
     *
     * @param rows   数据
     * @param writer 自定义写入器
     * @return 导出结果
     */
    public ExcelExportResult exportWithCustomWriter(List<OrderRow> rows, ExcelRowWriter<OrderRow> writer) {
        return ExcelExporterBuilder.of(new OrderBatchSource(rows), writer, OrderRow.class).export();
    }
}
