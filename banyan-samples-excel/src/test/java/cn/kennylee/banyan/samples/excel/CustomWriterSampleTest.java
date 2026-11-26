package cn.kennylee.banyan.samples.excel;

import cn.kennylee.banyan.commons.error.BanyanException;
import cn.kennylee.banyan.excel.exporter.ExcelExportResult;
import cn.kennylee.banyan.excel.exporter.ExcelRowWriter;
import cn.kennylee.banyan.excel.exporter.WriterContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 自定义行写入器样例：验证公共契约可脱离 FastExcel 使用，以及失败时清理临时文件。
 *
 * @author kennylee
 * @since 0.1.0
 */
class CustomWriterSampleTest extends AbstractExcelSampleTest {

    @Test
    @DisplayName("纯 JDK 写入器可完成导出")
    void exportsWithCustomPureJdkWriter() throws IOException {
        // given
        List<OrderRow> data = SampleOrders.of(10);

        // when
        ExcelExportResult result = exportService.exportWithCustomWriter(data, new SemicolonWriter());
        keep(result);

        // then
        List<String> lines = Files.readAllLines(result.file().toPath());
        assertEquals(10, lines.size());
        assertEquals("ORD-00001;客户0;100;PAID", lines.get(0));
    }

    @Test
    @DisplayName("写出失败时抛本库异常并清理临时文件")
    void cleansUpWhenWriterFails() {
        // given
        FailingWriter writer = new FailingWriter();

        // when
        BanyanException exception = assertThrows(BanyanException.class,
            () -> exportService.exportWithCustomWriter(SampleOrders.of(5), writer));

        // then
        assertEquals("banyan.excel.export-failed", exception.getErrorCode().messageKey());
        assertNotNull(writer.target);
        assertFalse(writer.target.exists());
    }

    /**
     * 纯 JDK 分号分隔写入器。
     */
    static final class SemicolonWriter implements ExcelRowWriter<OrderRow> {

        private BufferedWriter out;

        @Override
        public void open(WriterContext<OrderRow> context) throws IOException {
            this.out = Files.newBufferedWriter(context.target().toPath());
        }

        @Override
        public void write(List<OrderRow> rows) throws IOException {
            for (OrderRow row : rows) {
                out.write(row.getOrderNo() + ";" + row.getCustomer() + ";"
                    + row.getAmount() + ";" + row.getStatus());
                out.newLine();
            }
        }

        @Override
        public void nextSheet() {
        }

        @Override
        public void close() throws IOException {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * 写出即失败的写入器。
     */
    static final class FailingWriter implements ExcelRowWriter<OrderRow> {

        private File target;

        @Override
        public void open(WriterContext<OrderRow> context) {
            this.target = context.target();
        }

        @Override
        public void write(List<OrderRow> rows) throws IOException {
            throw new IOException("模拟写出失败");
        }

        @Override
        public void nextSheet() {
        }

        @Override
        public void close() {
        }
    }
}
