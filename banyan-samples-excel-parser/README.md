# banyan-samples-excel-parser

`banyan-excel-parser` 的可运行样例：通过 Spring MVC 上传 xlsx，按批次流式读取并返回结果。失败数据直接出现在 JSON 响应里，不依赖 Redis 或任何外部存储。

## 验证什么

| 接口 | 能力 |
|---|---|
| `POST /parse/xlsx` | 流式分批读取，返回成功行与转换失败行 |
| `POST /parse/batch-save` | 处理器逐批模拟存库，不保留批次数据 |
| `POST /parse/header-alias` | 表头别名把 `* 手机号` 映射到 `mobile` |
| `POST /parse/limit` | `maxRows` 触顶截断并标记 `truncated` |
| `POST /parse/unordered` | 关闭保序、并行处理，按批次行号还原文件顺序 |

## 启动

```bash
mvn -pl banyan-samples-excel-parser spring-boot:run
```

默认 `server.port=0`（随机端口）；需要固定端口时用 `SERVER_PORT` 覆盖：

```bash
SERVER_PORT=18767 mvn -pl banyan-samples-excel-parser spring-boot:run
```

## 接口调用

以固定端口 `18767` 为例：

```bash
curl -s -F 'file=@orders.xlsx' 'http://localhost:18767/parse/xlsx'
curl -s -F 'file=@orders.xlsx' 'http://localhost:18767/parse/batch-save'
curl -s -F 'file=@orders-star.xlsx' 'http://localhost:18767/parse/header-alias'
curl -s -F 'file=@orders.xlsx' 'http://localhost:18767/parse/limit?maxRows=4'
curl -s -F 'file=@orders.xlsx' 'http://localhost:18767/parse/unordered'
```

`/parse/xlsx` 的响应包含 `totalRows`、`batchCount`、`truncated`、`costMs`、`rows` 与 `errors`；`errors` 的元素带文件物理行号与原因。

## 关键配置项

`banyan-excel-parser` **没有** `banyan.*` 配置项：批次大小、并发度、在途批次上限、最大行数、sheet 与表头别名都在调用 `FastExcelParsers` 时显式设置。`application.yml` 只保留端口：

| 键 | 默认 | 说明 |
|---|---|---|
| `server.port` | `0` | 随机端口；可用 `SERVER_PORT` 覆盖 |

## 包结构

| 包 | 职责 |
|---|---|
| `controller` | HTTP 上传端点，把 `MultipartFile` 转成 `InputStream` |
| `service` | 把各解析场景收敛成方法 |
| `model` | `OrderRow`、`ParseResponse`、`RowErrorView`、`BatchSaveResponse` |
| `handler` | `InMemorySaveHandler`：逐批模拟存库，不保留数据 |
| `support` | 无（示例数据在测试基类生成） |

## 测试用例索引

| 测试类 | 场景 |
|---|---|
| `ParseEndpointsSampleTest` | 五个接口各一个用例，另含失败行收集 |

所有用例使用 `@SpringBootTest` + `MockMvc`，上传文件由测试内用 FastExcel 生成，结束后清理临时文件；不依赖外部服务。

## 测试

```bash
mvn -pl banyan-samples-excel-parser test
```

先确保 banyan 库已安装到本地仓库：

```bash
(cd ../banyan-starter && mvn clean install -DskipTests)
```
