package cn.kennylee.banyan.samples.excel.writer;

import cn.kennylee.banyan.excel.exporter.ExcelRowWriter;
import cn.kennylee.banyan.excel.exporter.WriterContext;
import cn.kennylee.banyan.samples.excel.model.OrderRow;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 写出即失败的写入器，用于演示失败时抛本库异常并清理临时文件。
 *
 * @author kennylee
 * @since 0.1.0
 */
public final class FailingOrderWriter implements ExcelRowWriter<OrderRow> {

    /** 写出目标，供测试核对失败后是否被清理。 */
    private File target;

    @Override
    public void open(WriterContext<OrderRow> context) {
        this.target = context.target();
    }

    /**
     * 获取写出目标。
     *
     * @return 写出目标，打开前为 {@code null}
     */
    public File getTarget() {
        return target;
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
