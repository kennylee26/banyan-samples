package cn.kennylee.banyan.samples.excel;

import cn.kennylee.banyan.excel.exporter.ExcelExportResult;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

/**
 * 样例测试基类：启动真实容器，并在每个用例结束后统一清理导出文件。
 *
 * @author kennylee
 * @since 0.1.0
 */
@SpringBootTest(properties = "samples.excel.demo=false")
abstract class AbstractExcelSampleTest {

    /** 导出服务。 */
    @Autowired
    protected OrderExportService exportService;

    /** 待清理的导出结果。 */
    private final List<ExcelExportResult> exportedResults = new ArrayList<>();

    /**
     * 登记导出结果，测试结束后统一删除。
     *
     * @param result 导出结果
     */
    protected void keep(ExcelExportResult result) {
        exportedResults.add(result);
    }

    @AfterEach
    void cleanupExportedFiles() {
        exportedResults.forEach(ExcelExportResult::deleteFile);
        exportedResults.clear();
    }
}
