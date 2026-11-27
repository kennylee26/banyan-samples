package cn.kennylee.banyan.samples.excel;

import cn.idev.excel.FastExcel;
import cn.idev.excel.read.listener.PageReadListener;
import cn.kennylee.banyan.samples.excel.model.OrderRow;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 样例测试共用工具：读回导出文件、统计 sheet 与读取首行表头。
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

}
