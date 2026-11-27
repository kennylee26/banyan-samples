package cn.kennylee.banyan.samples.excel;

import cn.kennylee.banyan.excel.exporter.ExcelExportResult;
import cn.kennylee.banyan.excel.exporter.fastexcel.FastExcelExporters;
import cn.kennylee.banyan.samples.excel.model.OrderRow;
import cn.kennylee.banyan.samples.excel.source.GeneratedOrderSource;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 大数据量探针：类名不以 Test 结尾，且需显式开关，默认不进常规套件。
 *
 * <p>手动执行：{@code mvn -pl banyan-samples-excel test -Dtest=LargeVolumeProbe -Dsamples.excel.large=true}，
 * 打印耗时、文件大小与峰值内存，并断言导入行数与 sheet 数。</p>
 *
 * @author kennylee
 * @since 0.1.0
 */
class LargeVolumeProbe {

    /** 日志器。 */
    private static final Logger LOGGER = LoggerFactory.getLogger(LargeVolumeProbe.class);

    /** 总行数。 */
    private static final int TOTAL_ROWS = 5_000_000;

    /** 批次大小。 */
    private static final int BATCH_SIZE = 5_000;

    /** 期望 sheet 数（5,000,000 / 1,048,575 向上取整）。 */
    private static final int EXPECTED_SHEETS = 5;

    @Test
    @EnabledIfSystemProperty(named = "samples.excel.large", matches = "true")
    void probeFiveMillionRows() throws Exception {
        AtomicLong peak = new AtomicLong(usedBytes());
        AtomicBoolean running = new AtomicBoolean(true);
        Thread sampler = new Thread(() -> {
            while (running.get()) {
                peak.accumulateAndGet(usedBytes(), Math::max);
                try {
                    Thread.sleep(20L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }, "memory-sampler");
        sampler.setDaemon(true);
        sampler.start();

        long start = System.currentTimeMillis();
        ExcelExportResult result = FastExcelExporters.xlsx(new GeneratedOrderSource(TOTAL_ROWS), OrderRow.class)
            .batchSize(BATCH_SIZE)
            .export();
        long costMs = System.currentTimeMillis() - start;

        running.set(false);
        sampler.join(1_000L);

        LOGGER.info("[probe] rows={}, costMs={}, fileMB={}, maxHeapMB={}, peakUsedMB={}",
            result.totalRecords(), costMs, result.file().length() / 1024 / 1024,
            Runtime.getRuntime().maxMemory() / 1024 / 1024, peak.get() / 1024 / 1024);
        try {
            assertEquals(TOTAL_ROWS, result.totalRecords());
            assertTrue(result.file().length() > 0);
            assertEquals(EXPECTED_SHEETS, sheetCount(result.file()));
        } finally {
            result.deleteFile();
        }
    }

    private static int sheetCount(File file) throws Exception {
        try (OPCPackage pkg = OPCPackage.open(file)) {
            XSSFReader reader = new XSSFReader(pkg);
            Iterator<InputStream> sheets = reader.getSheetsData();
            int count = 0;
            while (sheets.hasNext()) {
                try (InputStream ignored = sheets.next()) {
                    count++;
                }
            }
            return count;
        }
    }

    private static long usedBytes() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
}
