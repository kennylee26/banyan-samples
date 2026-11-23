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

- `SigningHttpClients.builder(signer).build()`：Apache HttpClient 5
- `SigningRestTemplates.create(signer)`：Spring RestTemplate

两者都会自动加密 body、计算签名并写入五个 header。

## 测试用例

| 测试类 | 覆盖 |
|---|---|
| `SignClientEndToEndTest` | 两个客户端加密请求、空 body 请求、错误密钥 401 |
| `PlainModeEndToEndTest` | 提供方 `encrypted=false` + 客户端 `plainBody()` |
| `CustomHeaderEndToEndTest` | 自定义五个 header 名 |
| `ContextPathEndToEndTest` | `server.servlet.context-path=/api` + 客户端 `basePath("/api")` |

## 测试

```bash
mvn -pl banyan-samples-sign test
```

先确保 `banyan-sign` 模块已安装到本地仓库：

```bash
mvn -f ../../banyan-starter/banyan-sign/pom.xml install -DskipTests
```
