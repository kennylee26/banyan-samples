# banyan-samples-excel

`banyan-excel` 的可运行样例：通过 Spring MVC 接口触发导出，每个接口对应库的一项能力，同步导出后直接下载文件。

## 验证什么

| 接口 | 能力 |
|---|---|
| `GET /export/xlsx` | 基础分页批次导出 xlsx，按 `batchSize` 逐批取数 |
| `GET /export/template` | 传入模板时保留模板表头、数据写在表头之后，不重复表头 |
| `GET /export/sheets` | 超过 `sheetCapacity` 时自动分 sheet，使用方不指定 sheet 编号 |
| `GET /export/csv` | CSV 单文件，不按 sheet 容量拆分 |
| `GET /export/cursor` | 游标（keyset）分页：实现 `lastIdOf` 后库把上一批末条 ID 带进下一批 |
| `GET /export/limit` | 显式上限触顶时截断，响应头 `X-Export-Truncated` 标记 |
| `GET /export/custom` | 公共契约可脱离 FastExcel：纯 JDK 写入器完成导出 |
| `GET /export/large` | 大数据量导出：按需生成数据、不驻留全量，自动分 sheet |
| `GET /export/fail` | 写出失败时抛 `BanyanException` 并清理临时文件，HTTP 返回 500 |

另外 `BatchSourceSampleTest` 直接验证「库按批次大小逐批取数」（库内部行为，不经过 HTTP）。

## 启动

```bash
mvn -pl banyan-samples-excel spring-boot:run
```

默认 `server.port=0`（随机端口）；需要固定端口时用 `SERVER_PORT` 覆盖：

```bash
SERVER_PORT=18769 mvn -pl banyan-samples-excel spring-boot:run
```

启动后还会导出一次 100 行示例并打印路径；关闭演示：

```bash
mvn -pl banyan-samples-excel spring-boot:run -Dspring-boot.run.arguments=--samples.excel.demo=false
```

## 接口调用

以固定端口 `18769` 为例，`-OJ` 让 curl 按 `Content-Disposition` 保存文件名：

```bash
curl -OJ 'http://localhost:18769/export/xlsx?rows=100'
curl -OJ 'http://localhost:18769/export/template?rows=50'
curl -OJ 'http://localhost:18769/export/sheets?rows=100&sheetCapacity=30'
curl -OJ 'http://localhost:18769/export/csv?rows=100'
curl -OJ 'http://localhost:18769/export/cursor?rows=100'
curl -OJ 'http://localhost:18769/export/limit?rows=100&maxRecords=40'
curl -OJ 'http://localhost:18769/export/custom?rows=10'
curl -OJ 'http://localhost:18769/export/large?rows=5000000'
curl -s -o /dev/null -w '%{http_code}\n' 'http://localhost:18769/export/fail'
```

## 大数据量

`/export/large` 默认 500 万行，按需生成数据、不驻留全量，导出时自动分 sheet（默认容量 1,048,575，500 万行约 5 个 sheet）。实测（8GB 堆）：

```
rows=5,000,000  costMs≈24,800  fileMB≈100  peakUsedMB≈317
```

约 25 秒、100MB 文件、峰值已用约 317MB，未 OOM。这条路径仅用于演示，常规测试不跑；需要验证时手动执行：

```bash
mvn -pl banyan-samples-excel test -Dtest=LargeVolumeProbe -Dsamples.excel.large=true
```

`LargeVolumeProbe` 默认被 `@EnabledIfSystemProperty` 跳过，只有显式传 `-Dsamples.excel.large=true` 才执行；导出结束后临时文件会被删除。

`/export/limit` 可再看响应头确认截断：

```bash
curl -sI 'http://localhost:18769/export/limit?rows=100&maxRecords=40' | grep -i x-export-truncated
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
| `support` | 示例订单数据与模板生成 |

## 测试用例索引

| 测试类 | 场景 |
|---|---|
| `XlsxExportSampleTest` | `/export/xlsx` 分页下载、`/export/template` 模板、`/export/sheets` 自动分 sheet |
| `CsvExportSampleTest` | `/export/csv` 单文件下载且行数一致 |
| `PaginationAndLimitSampleTest` | `/export/cursor` 游标分页、`/export/limit` 截断响应头、`/export/xlsx?rows=0` 空数据含 sheet 与表头 |
| `CustomWriterSampleTest` | `/export/custom` 纯 JDK 写入器、`/export/fail` 返回 500 |
| `BatchSourceSampleTest` | 库按批次大小逐批取数（直接验证） |
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
