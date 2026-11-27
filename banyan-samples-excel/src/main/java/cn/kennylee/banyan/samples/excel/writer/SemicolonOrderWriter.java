package cn.kennylee.banyan.samples.excel.writer;

import cn.kennylee.banyan.excel.exporter.ExcelRowWriter;
import cn.kennylee.banyan.excel.exporter.WriterContext;
import cn.kennylee.banyan.samples.excel.model.OrderRow;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

/**
 * 分号分隔的纯 JDK 写入器，验证公共契约可脱离 FastExcel 使用。
 *
 * @author kennylee
 * @since 0.1.0
 */
public final class SemicolonOrderWriter implements ExcelRowWriter<OrderRow> {

    /** 输出流。 */
    private BufferedWriter out;

    @Override
    public void open(WriterContext<OrderRow> context) throws IOException {
        this.out = Files.newBufferedWriter(context.target().toPath(), StandardCharsets.UTF_8);
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
