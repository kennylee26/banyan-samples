package cn.kennylee.banyan.samples.excel;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * 样例测试基类：启动 Spring 上下文与 MockMvc，并在每个用例结束后清理临时文件。
 *
 * @author kennylee
 * @since 0.1.0
 */
@SpringBootTest(properties = "samples.excel.demo=false")
@AutoConfigureMockMvc
abstract class AbstractExcelSampleTest {

    /** MockMvc。 */
    @Autowired
    protected MockMvc mockMvc;

    /** 本用例生成的临时文件。 */
    private final List<File> tempFiles = new ArrayList<>();

    /**
     * 把响应体写成临时文件，便于读回校验；用例结束后统一删除。
     *
     * @param suffix  文件后缀
     * @param content 响应体
     * @return 临时文件
     * @throws IOException 写入失败
     */
    protected File writeTemp(String suffix, byte[] content) throws IOException {
        File file = File.createTempFile("sample-", suffix);
        Files.write(file.toPath(), content);
        tempFiles.add(file);
        return file;
    }

    @AfterEach
    void cleanupTempFiles() {
        tempFiles.forEach(File::delete);
        tempFiles.clear();
    }
}
