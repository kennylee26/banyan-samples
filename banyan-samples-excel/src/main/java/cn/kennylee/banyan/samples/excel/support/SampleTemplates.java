package cn.kennylee.banyan.samples.excel.support;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 样例模板工厂：生成只有表头的 xlsx 模板。
 *
 * @author kennylee
 * @since 0.1.0
 */
public final class SampleTemplates {

    private SampleTemplates() {
    }

    /**
     * 生成只含一行表头的 xlsx 模板。
     *
     * @param headers 表头
     * @return 模板输入流
     * @throws IOException 生成失败
     */
    public static InputStream headerOnly(String... headers) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet();
            org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}
