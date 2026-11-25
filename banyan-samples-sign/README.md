# banyan-samples-sign

`banyan-sign` 的使用示例：同一个 Spring Boot 应用里既提供受签名保护的接口，也用两个客户端工具调用它。

## 启动

```bash
SERVER_PORT=18767 mvn spring-boot:run
```

`server.port` 默认 `0`（随机），启动日志会打印实际端口；需要固定时用 `SERVER_PORT`。

## 提供方

`/openapi/orders` 与 `/openapi/orders/ping` 标注了 `@OpenApiSigned`：

- 验签通过后，Controller 拿到的是解密后的明文
- 签名错误、过期、重放、未知 keyId、解密失败统一返回 401

密钥在 `application.yml`：

```yaml
banyan:
  openapi-sign:
    keys:
      app1: secret-for-tests
```

## 客户端

- `SigningHttpRequests.sign(signer, post)`：Apache HttpClient 5，原地签名，client 自己构造
- `SigningRestTemplates.attach(restTemplate, signer)`：Spring RestTemplate，挂到已有模板
- `SigningJdkHttpRequests.sign(signer, request, body)`：JDK HttpClient，返回已签名请求

三者都会自动加密 body、计算签名并写入五个 header。想全自动时也可以用
`SigningHttpClients.create(signer)` / `SigningRestTemplates.create(signer)`。

## 关键配置项

`application.yml` 显式列出 `banyan-sign-openapi` 的相关键（与 [`application.yml`](src/main/resources/application.yml) 一致）：

| 配置项 | 样例值 | 默认值 / 说明 |
|---|---|---|
| `banyan.openapi-sign.enabled` | `true` | 模块总开关，默认 `true` |
| `banyan.openapi-sign.encrypted` | `true` | 是否要求 body 加密，默认 `true` |
| `banyan.openapi-sign.window` | `5m` | 验签时间窗口，默认 `5m` |
| `banyan.openapi-sign.reveal-reason` | `false` | 是否返回具体失败原因，默认 `false` |
| `banyan.openapi-sign.keys.app1` | `secret-for-tests` | 样例联调用固定密钥 |

其余键（`sign-algorithm`、`cipher`、`headers.*`、`error.*`）保持默认，完整说明见 `banyan-sign-openapi` 模块的 `banyan-sign-openapi.yml`。

## 测试用例

| 测试类 | 覆盖 |
|---|---|
| `SignClientEndToEndTest` | HttpClient5 / RestTemplate / JDK HttpClient 加密请求、空 body、错误密钥 401 |
| `PlainModeEndToEndTest` | 提供方 `encrypted=false` + 客户端 `plainBody()` |
| `CustomHeaderEndToEndTest` | 自定义五个 header 名 |
| `ContextPathEndToEndTest` | `server.servlet.context-path=/api` + 客户端 `basePath("/api")` |
| `LogLeakageTest` | TRACE 全开时日志不泄漏 secret / 明文 / 完整签名 |
| `ProtocolContractTest` | 客户端签名结果通过服务端 `ReplayGuard`，默认 header 名与服务端一致 |

## 测试

```bash
mvn -pl banyan-samples-sign test
```

先确保 `banyan-sign` 模块已安装到本地仓库：

```bash
(cd ../../banyan-starter && mvn clean install -DskipTests)
```
