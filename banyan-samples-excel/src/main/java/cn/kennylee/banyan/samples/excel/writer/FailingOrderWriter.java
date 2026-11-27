package cn.kennylee.banyan.samples.excel.writer;

import cn.kennylee.banyan.excel.exporter.ExcelRowWriter;
import cn.kennylee.banyan.excel.exporter.WriterContext;
import cn.kennylee.banyan.samples.excel.model.OrderRow;

import java.io.IOException;
import java.util.List;

/**
 * 写出即失败的写入器，用于演示失败时抛本库异常并清理临时文件。
 *
 * @author kennylee
 * @since 0.1.0
 */
public final class FailingOrderWriter implements ExcelRowWriter<OrderRow> {

    @Override
    public void open(WriterContext<OrderRow> context) {
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
