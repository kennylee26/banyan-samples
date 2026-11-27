package cn.kennylee.banyan.samples.excel;

import cn.kennylee.banyan.commons.error.BanyanException;
import cn.kennylee.banyan.excel.exporter.ExcelExporterBuilder;
import cn.kennylee.banyan.samples.excel.model.OrderRow;
import cn.kennylee.banyan.samples.excel.source.OrderBatchSource;
import cn.kennylee.banyan.samples.excel.support.SampleOrders;
import cn.kennylee.banyan.samples.excel.writer.FailingOrderWriter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 失败场景样例：直接调用库，断言异常类型与临时文件清理。
 *
 * @author kennylee
 * @since 0.1.0
 */
class ExportFailureSampleTest {

    @Test
    @DisplayName("写出失败抛本库异常并清理临时文件")
    void failsAndCleansUp() {
        // given
        FailingOrderWriter writer = new FailingOrderWriter();

        // when
        BanyanException exception = assertThrows(BanyanException.class,
            () -> ExcelExporterBuilder.of(new OrderBatchSource(SampleOrders.of(5)), writer, OrderRow.class)
                .export());

        // then
        assertEquals("banyan.excel.export-failed", exception.getErrorCode().messageKey());
        assertNotNull(writer.getTarget());
        assertFalse(writer.getTarget().exists(), "失败后临时文件应被清理");
    }
}
