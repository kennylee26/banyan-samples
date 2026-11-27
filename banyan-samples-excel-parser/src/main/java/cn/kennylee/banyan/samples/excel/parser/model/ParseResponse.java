package cn.kennylee.banyan.samples.excel.parser.model;

import java.util.List;

/**
 * 解析接口响应。
 *
 * @param totalRows  实际读取行数
 * @param batchCount 批次数
 * @param truncated  是否因最大行数截断
 * @param costMs     耗时毫秒
 * @param rows       成功读取的行
 * @param errors     行级错误
 * @author kennylee
 * @since 0.1.0
 */
public record ParseResponse(int totalRows, int batchCount, boolean truncated, long costMs,
                            List<OrderRow> rows, List<RowErrorView> errors) {

    /**
     * 复制列表，避免外部修改。
     */
    public ParseResponse {
        rows = rows == null ? List.of() : List.copyOf(rows);
        errors = errors == null ? List.of() : List.copyOf(errors);
    }
}
