package cn.kennylee.banyan.samples.excel.parser.model;

/**
 * 响应中的行级错误。
 *
 * @param rowIndex 文件中的物理行号
 * @param message  错误说明
 * @author kennylee
 * @since 0.1.0
 */
public record RowErrorView(int rowIndex, String message) {
}
