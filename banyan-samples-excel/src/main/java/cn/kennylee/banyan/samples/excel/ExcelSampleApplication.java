package cn.kennylee.banyan.samples.excel;

import cn.kennylee.banyan.excel.exporter.ExcelExportResult;
import cn.kennylee.banyan.samples.excel.service.OrderExportService;
import cn.kennylee.banyan.samples.excel.support.SampleOrders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * Excel 导出样例应用：启动时导出一次示例文件并打印路径。
 *
 * @author kennylee
 * @since 0.1.0
 */
@SpringBootApplication
public class ExcelSampleApplication {

    /** 日志器。 */
    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelSampleApplication.class);

    /**
     * 启动应用。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(ExcelSampleApplication.class, args);
    }

    /**
     * 启动后导出一次示例，方便人工验证。
     *
     * @param exportService 导出服务
     * @return 启动回调
     */
    @Bean
    @ConditionalOnProperty(name = "samples.excel.demo", havingValue = "true", matchIfMissing = true)
    CommandLineRunner excelDemoRunner(OrderExportService exportService) {
        return args -> {
            ExcelExportResult result = exportService.exportXlsx(SampleOrders.of(100), 20);
            LOGGER.info("[samples-excel] exported {} rows to {}", result.totalRecords(),
                result.file().getAbsolutePath());
            result.file().deleteOnExit();
        };
    }
}
