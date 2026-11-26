# banyan-samples-excel

`banyan-excel` 的可运行样例：验证分页批次导出、模板填充、自动分 sheet、CSV 旁路、游标分页、上限截断与自定义写入器在真实 Spring Boot 应用里的行为。

## 验证什么

- 使用方实现 `BatchSource` 后，库按批次大小逐批取数与写出（用例断言了 4 批 30/30/30/10），导出完整数据。
- 传入模板时保留模板表头、数据写在表头之后，不重复写出表头；无模板时按表头类写表头。
- 超过 `sheetCapacity` 时自动分 sheet，使用方不需要指定 sheet 编号。
- CSV 格式产出单个 `.csv` 文件，不按 sheet 容量拆分，可被标准读取器读回。
- 游标（keyset）分页：使用方实现 `lastIdOf` 后，库把上一批末条 ID 带进下一批。
- 显式设置最大条数时静默截断，并在结果中标记 `truncated=true`；未设上限时不标记。
- 空数据默认仍产出可读文件（含 1 个 sheet 与表头）。
- 公共契约可脱离 FastExcel 使用：纯 JDK 写入器也能完成导出；写出失败时抛 `BanyanException` 并清理临时文件。

## 启动

```bash
mvn -pl banyan-samples-excel spring-boot:run
```

启动后默认导出一次 100 行示例并打印文件路径。关闭启动演示：

```bash
mvn -pl banyan-samples-excel spring-boot:run -Dspring-boot.run.arguments=--samples.excel.demo=false
```

本样例不提供服务端口，是纯调用式工具库的演示；`banyan-excel` 本身不注册 Spring bean，也没有自动装配。

## 关键配置项

`banyan-excel` **没有** `banyan.*` 配置项：批次大小、最大条数、sheet 容量与输出格式都在调用 `ExcelExporterBuilder` 时显式设置。`application.yml` 只保留样例自身的演示开关：

| 键 | 默认 | 说明 |
|---|---|---|
| `samples.excel.demo` | `true` | 启动时是否导出一次示例；测试里置 `false` 关闭 |

完整 API 见 `banyan-excel-exporter` 模块的 README；真实写出能力见 `banyan-excel-exporter-fastexcel` 模块的 README。

## 测试用例索引

| 测试类 | 场景 |
|---|---|
| `XlsxExportSampleTest` | 分页导出可读回、带模板不重复表头、超过容量自动分 sheet 且数据完整 |
| `CsvExportSampleTest` | CSV 写出 `.csv`、可读回且行数一致 |
| `PaginationAndLimitSampleTest` | 游标分页无重复、上限截断并标记、空数据产出可读文件 |
| `CustomWriterSampleTest` | 纯 JDK 写入器完成导出、写出失败抛本库异常并清理临时文件 |

所有用例使用 `@SpringBootTest` 启动真实容器，读取导出数据用 FastExcel、统计 sheet 元数据用 POI（均由实现模块传递引入），不依赖外部服务。

模板场景只验证「保留模板表头、数据写在表头之后」，不承诺数据行继承模板单元格样式——这取决于 FastExcel 的模板行为。

## 测试

```bash
mvn -pl banyan-samples-excel test
```

先确保 banyan 库已安装到本地仓库：

```bash
(cd ../../banyan-starter && mvn clean install -DskipTests)
```
