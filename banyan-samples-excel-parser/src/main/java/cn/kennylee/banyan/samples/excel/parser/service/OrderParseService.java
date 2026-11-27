package cn.kennylee.banyan.samples.excel.parser.service;

import cn.kennylee.banyan.excel.parser.ParseResult;
import cn.kennylee.banyan.excel.parser.ReadBatch;
import cn.kennylee.banyan.excel.parser.RowError;
import cn.kennylee.banyan.excel.parser.fastexcel.FastExcelParsers;
import cn.kennylee.banyan.samples.excel.parser.handler.InMemorySaveHandler;
import cn.kennylee.banyan.samples.excel.parser.model.BatchSaveResponse;
import cn.kennylee.banyan.samples.excel.parser.model.OrderRow;
import cn.kennylee.banyan.samples.excel.parser.model.ParseResponse;
import cn.kennylee.banyan.samples.excel.parser.model.RowErrorView;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 把解析场景收敛成方法，供上传接口调用。
 *
 * @author kennylee
 * @since 0.1.0
 */
@Service
public class OrderParseService {

    /**
     * 流式读取并返回成功行与失败行。
     *
     * @param source 上传流
     * @return 解析响应
     */
    public ParseResponse parseXlsx(InputStream source) {
        List<OrderRow> rows = new ArrayList<>();
        // 空回调仅用于启用「转换失败记录并跳过」模式；失败行会出现在 ParseResult.errors
        ParseResult result = FastExcelParsers.xlsx(OrderRow.class)
            .onConvertError(error -> {
            })
            .read(source, batch -> rows.addAll(batch.rows()));
        return toResponse(result, rows);
    }

    /**
     * 模拟批量存库：处理器只累加计数，不保留批次数据。
     *
     * @param source 上传流
     * @return 批量存库响应
     */
    public BatchSaveResponse batchSave(InputStream source) {
        InMemorySaveHandler handler = new InMemorySaveHandler();
        ParseResult result = FastExcelParsers.xlsx(OrderRow.class).read(source, handler);
        return new BatchSaveResponse(handler.savedRows(), result.batchCount());
    }

    /**
     * 用表头别名读取带星号表头的文件。
     *
     * @param source 上传流
     * @return 解析响应
     */
    public ParseResponse parseWithHeaderAlias(InputStream source) {
        List<OrderRow> rows = new ArrayList<>();
        ParseResult result = FastExcelParsers.xlsx(OrderRow.class)
            .headerAlias("* 手机号", "mobile")
            .read(source, batch -> rows.addAll(batch.rows()));
        return toResponse(result, rows);
    }

    /**
     * 限制最大读取行数，触顶时截断。
     *
     * @param source  上传流
     * @param maxRows 最大行数
     * @return 解析响应
     */
    public ParseResponse parseWithLimit(InputStream source, int maxRows) {
        List<OrderRow> rows = new ArrayList<>();
        ParseResult result = FastExcelParsers.xlsx(OrderRow.class)
            .maxRows(maxRows)
            .read(source, batch -> rows.addAll(batch.rows()));
        return toResponse(result, rows);
    }

    /**
     * 关闭保序、并行处理批次，再用行号把结果还原成文件顺序。
     *
     * @param source 上传流
     * @return 解析响应
     */
    public ParseResponse parseUnordered(InputStream source) {
        List<ReadBatch<OrderRow>> batches = new CopyOnWriteArrayList<>();
        ParseResult result = FastExcelParsers.xlsx(OrderRow.class)
            .ordered(false)
            .parallelism(4)
            .maxPendingBatches(8)
            .batchSize(50)
            .read(source, batches::add);
        // 只依赖批次起始行号单调，不假定批内行号连续（空行或失败行会被跳过）
        List<OrderRow> rows = batches.stream()
            .sorted(Comparator.comparingInt(ReadBatch::firstRowIndex))
            .flatMap(batch -> batch.rows().stream())
            .toList();
        return toResponse(result, rows);
    }

    private static ParseResponse toResponse(ParseResult result, List<OrderRow> rows) {
        return new ParseResponse(result.totalRows(), result.batchCount(), result.truncated(),
            result.spentTime().toMillis(), rows, views(result.errors()));
    }

    private static List<RowErrorView> views(List<RowError> errors) {
        return errors.stream()
            .map(error -> new RowErrorView(error.rowIndex(), error.message()))
            .toList();
    }
}
