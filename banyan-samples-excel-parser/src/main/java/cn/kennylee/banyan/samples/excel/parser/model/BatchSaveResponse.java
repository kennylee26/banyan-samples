package cn.kennylee.banyan.samples.excel.parser.model;

/**
 * 批量存库样例响应。
 *
 * @param savedRows  已保存行数
 * @param batchCount 批次数
 * @author kennylee
 * @since 0.1.0
 */
public record BatchSaveResponse(int savedRows, int batchCount) {
}
