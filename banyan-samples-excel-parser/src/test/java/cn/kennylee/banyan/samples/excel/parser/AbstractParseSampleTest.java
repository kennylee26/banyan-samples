package cn.kennylee.banyan.samples.excel.parser;

import cn.idev.excel.FastExcel;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 解析样例测试基类：生成上传文件、清理临时文件。
 *
 * @author kennylee
 * @since 0.1.0
 */
@SpringBootTest
@AutoConfigureMockMvc
abstract class AbstractParseSampleTest {

    protected static final String XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    @Autowired
    protected MockMvc mockMvc;

    private final List<File> tempFiles = new ArrayList<>();

    @AfterEach
    void cleanup() {
        tempFiles.forEach(File::delete);
    }

    protected static List<WriteRow> orders(int count) {
        List<WriteRow> rows = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            rows.add(new WriteRow("ORD-" + i, String.format(Locale.ROOT, "1380000%04d", i), "10.50"));
        }
        return rows;
    }

    protected MockMultipartFile xlsxFile(List<WriteRow> rows) {
        File file = tempFile(".xlsx");
        FastExcel.write(file, WriteRow.class).sheet("订单").doWrite(rows);
        return multipart(file, XLSX);
    }

    protected MockMultipartFile badAmountFile() {
        List<WriteRow> rows = List.of(
            new WriteRow("ORD-1", "13800000001", "abc"),
            new WriteRow("ORD-2", "13800000002", "9.90"));
        return xlsxFile(rows);
    }

    protected MockMultipartFile starHeaderFile(String mobile) {
        File file = tempFile(".xlsx");
        StarWriteRow row = new StarWriteRow();
        row.setMobile(mobile);
        FastExcel.write(file, StarWriteRow.class).sheet("用户").doWrite(List.of(row));
        return multipart(file, XLSX);
    }

    private MockMultipartFile multipart(File file, String contentType) {
        try {
            return new MockMultipartFile("file", file.getName(), contentType, Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            throw new IllegalStateException("读取临时文件失败", e);
        }
    }

    private File tempFile(String suffix) {
        try {
            File file = File.createTempFile("parse-sample-", suffix);
            tempFiles.add(file);
            return file;
        } catch (IOException e) {
            throw new IllegalStateException("创建临时文件失败", e);
        }
    }

    /**
     * 写出用的订单行。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WriteRow {

        @ExcelProperty("订单号")
        private String orderNo;

        @ExcelProperty("手机号")
        private String mobile;

        @ExcelProperty("金额")
        private String amount;
    }

    /**
     * 带星号表头的写出用行。
     */
    @Data
    public static class StarWriteRow {

        @ExcelProperty("* 手机号")
        private String mobile;
    }
}
