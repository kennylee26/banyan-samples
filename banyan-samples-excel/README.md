# banyan-samples-excel

`banyan-excel` 的可运行样例：通过 Spring MVC 接口触发导出，每个接口对应库的一项能力，同步导出后直接下载文件。

## 验证什么

| 接口 | 能力 |
|---|---|
| `GET /export/xlsx` | 基础分页批次导出 xlsx，按 `batchSize` 逐批取数 |
| `GET /export/template` | 从 classpath 读取 `templates/orders-template.xlsx`，保留模板表头、数据写在表头之后 |
| `GET /export/sheets` | 超过 `sheetCapacity` 时自动分 sheet，使用方不指定 sheet 编号 |
| `GET /export/csv` | CSV 单文件，不按 sheet 容量拆分 |
| `GET /export/cursor` | 游标（keyset）分页：实现 `lastIdOf` 后库把上一批末条 ID 带进下一批 |
| `GET /export/limit` | 显式上限触顶时截断，响应头 `X-Export-Truncated` 标记 |
| `GET /export/custom` | 用纯 JDK 写入器演示公共契约（不依赖 FastExcel 写出） |
| `GET /export/large` | 大数据量导出：按需生成数据、不驻留全量，自动分 sheet |
| `GET /export/fail` | 写出失败时抛 `BanyanException` 并清理临时文件，HTTP 返回 500 |

另外 `BatchSourceSampleTest` 直接验证「库按批次大小逐批取数」（库内部行为，不经过 HTTP）。

## 启动

```bash
mvn -pl banyan-samples-excel spring-boot:run
```

默认 `server.port=0`（随机端口）；需要固定端口时用 `SERVER_PORT` 覆盖：

```bash
SERVER_PORT=18768 mvn -pl banyan-samples-excel spring-boot:run
```

启动后还会导出一次 100 行示例并打印路径；关闭演示：

```bash
mvn -pl banyan-samples-excel spring-boot:run -Dspring-boot.run.arguments=--samples.excel.demo=false
```

## 接口调用

以固定端口 `18768` 为例，`-OJ` 让 curl 按 `Content-Disposition` 保存文件名：

```bash
curl -OJ 'http://localhost:18768/export/xlsx?rows=100'
curl -OJ 'http://localhost:18768/export/template?rows=50'
curl -OJ 'http://localhost:18768/export/sheets?rows=100&sheetCapacity=30'
curl -OJ 'http://localhost:18768/export/csv?rows=100'
curl -OJ 'http://localhost:18768/export/cursor?rows=100'
curl -OJ 'http://localhost:18768/export/limit?rows=100&maxRecords=40'
curl -OJ 'http://localhost:18768/export/custom?rows=10'
curl -OJ 'http://localhost:18768/export/large?rows=5000000'
curl -s -o /dev/null -w '%{http_code}\n' 'http://localhost:18768/export/fail'
```

## 模板怎么用

模板是一个普通 xlsx 文件，样例放在 `src/main/resources/templates/orders-template.xlsx`：

- 第一个 sheet 第一行是表头：`订单编号 / 客户名称 / 订单金额 / 订单状态`（文案与 `@ExcelProperty` 不同，用于证明模板确实生效）；
- 表头带加粗、浅灰背景、列宽，并冻结首行（可自行替换成业务模板）；
- **列顺序要与 `OrderRow` 的 `@ExcelProperty` 字段顺序一致**——数据按字段顺序写进模板对应列，模板表头只负责显示；
- 带模板导出时库调用 `needHead(false)`，数据写在模板表头之后，不重复表头。

接口实现（`controller/OrderExportController#template`）：

```java
try (InputStream template = getClass().getResourceAsStream("/templates/orders-template.xlsx")) {
    ExcelExportResult result = exportService.exportWithTemplate(SampleOrders.of(rows), template);
    download(result, "orders-template.xlsx", XLSX, response);
}
```

真实业务里模板可以来自 classpath、上传文件或对象存储，只要拿到 `InputStream`：

```java
ExcelExportResult result = FastExcelExporters.xlsx(source, OrderRow.class)
    .template(templateStream)   // 模板流由库在导出结束时关闭
    .batchSize(500)
    .export();
```

边界：

- 表头类仍要传（决定字段 ↔ 列映射），模板只决定样式与表头文案；
- 自动切 sheet 时新 sheet 不继承模板样式、也不额外写表头；要每个 sheet 都有表头，请用无模板模式；
- CSV 忽略模板（无样式、无 sheet）。

## 大数据量

`/export/large` 默认 500 万行，按需生成数据、不驻留全量，导出时自动分 sheet（默认容量 1,048,575，500 万行约 5 个 sheet）。响应头 `X-Export-Rows` 返回实际导出行数。

耗时与文件大小随机器和堆变化（量级为几十秒、上百 MB），仅用于演示，常规测试不跑；需要验证时手动执行：

```bash
mvn -pl banyan-samples-excel test -Dtest=LargeVolumeProbe -Dsamples.excel.large=true
```

`LargeVolumeProbe` 默认被 `@EnabledIfSystemProperty` 跳过，只有显式传 `-Dsamples.excel.large=true` 才执行；导出结束后临时文件会被删除。

`/export/limit` 可再看响应头确认截断：

```bash
curl -sI 'http://localhost:18768/export/limit?rows=100&maxRecords=40' | grep -i x-export-truncated
```

## 关键配置项

`banyan-excel` **没有** `banyan.*` 配置项：批次大小、最大条数、sheet 容量与输出格式都在调用 `ExcelExporterBuilder` 时显式设置。`application.yml` 只保留端口与样例自身开关：

| 键 | 默认 | 说明 |
|---|---|---|
| `server.port` | `0` | 随机端口；可用 `SERVER_PORT` 覆盖 |
| `samples.excel.demo` | `true` | 启动时是否导出一次示例；测试里置 `false` 关闭 |

完整 API 见 `banyan-excel-exporter` 模块的 README；真实写出能力见 `banyan-excel-exporter-fastexcel` 模块的 README。

## 包结构

| 包 | 职责 |
|---|---|
| `controller` | HTTP 导出端点，同步导出后写回下载响应 |
| `service` | 把各导出场景收敛成方法 |
| `model` | `OrderRow` 导出行（`@ExcelProperty` 列定义） |
| `source` | `BatchSource` 的两种实现：页码分页与游标分页 |
| `writer` | 自定义行写入器：纯 JDK 成功写出与失败写出 |
| `support` | 示例订单数据 |

## 测试用例索引

| 测试类 | 场景 |
|---|---|
| `XlsxExportSampleTest` | `/export/xlsx` 分页下载、`/export/template` 模板、`/export/sheets` 自动分 sheet |
| `CsvExportSampleTest` | `/export/csv` 单文件下载且行数一致 |
| `PaginationAndLimitSampleTest` | `/export/cursor` 游标分页、`/export/limit` 截断响应头、`/export/xlsx?rows=0` 空数据含 sheet 与表头 |
| `CustomWriterSampleTest` | `/export/custom` 纯 JDK 写入器、`/export/fail` 返回 500 |
| `BatchSourceSampleTest` | 库按批次大小逐批取数（直接验证） |
| `LargeEndpointSampleTest` | `/export/large` 行数响应头与导出数据完整 |
| `ExportFailureSampleTest` | 写出失败抛 `BanyanException` 并清理临时文件（直接验证） |
| `LargeVolumeProbe` | 500 万行导出（手动执行，默认不进常规套件） |

所有 HTTP 用例使用 `@SpringBootTest` + `MockMvc`，读取导出数据用 FastExcel、统计 sheet 元数据用 POI（均由实现模块传递引入），不依赖外部服务。

模板场景只验证「保留模板表头、数据写在表头之后」，不承诺数据行继承模板单元格样式——这取决于 FastExcel 的模板行为。

## 测试

```bash
mvn -pl banyan-samples-excel test
```

先确保 banyan 库已安装到本地仓库：

```bash
(cd ../../banyan-starter && mvn clean install -DskipTests)
```
