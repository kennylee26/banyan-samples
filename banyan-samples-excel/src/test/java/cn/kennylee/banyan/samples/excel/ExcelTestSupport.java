package cn.kennylee.banyan.samples.excel;

import cn.idev.excel.FastExcel;
import cn.idev.excel.read.listener.PageReadListener;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 样例测试共用工具：读回导出文件、统计 sheet、生成最小模板。
 *
 * @author kennylee
 * @since 0.1.0
 */
final class ExcelTestSupport {

    private ExcelTestSupport() {
    }

    static List<OrderRow> read(File file) {
        List<OrderRow> rows = new ArrayList<>();
        FastExcel.read(file, OrderRow.class, new PageReadListener<OrderRow>(rows::addAll)).sheet().doReadSync();
        return rows;
    }

    static int sheetCount(File file) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(file)) {
            return workbook.getNumberOfSheets();
        }
    }

    static String firstHeader(File file) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(file)) {
            return workbook.getSheetAt(0).getRow(0).getCell(0).getStringCellValue();
        }
    }

    static List<OrderRow> readAllSheets(File file) throws Exception {
        int sheets = sheetCount(file);
        List<OrderRow> rows = new ArrayList<>();
        for (int i = 0; i < sheets; i++) {
            FastExcel.read(file, OrderRow.class, new PageReadListener<OrderRow>(rows::addAll))
                .sheet(i)
                .doReadSync();
        }
        return rows;
    }

    static File templateWithHeader() throws IOException {
        File template = File.createTempFile("template-", ".xlsx");
        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream out = new FileOutputStream(template)) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet();
            org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("订单号");
            header.createCell(1).setCellValue("客户");
            header.createCell(2).setCellValue("金额");
            header.createCell(3).setCellValue("状态");
            workbook.write(out);
        }
        return template;
    }
}
