# banyan-samples

[English](README.md) | [简体中文](README.zh-CN.md)

Runnable examples and integration tests for [banyan](https://github.com/kennylee26/banyan), a set of reusable Spring Boot infrastructure libraries. This repository is the usage showcase for banyan — it is not published and is not itself a library.

## Modules

| Module | What it shows |
|---|---|
| [`banyan-samples-mq/`](banyan-samples-mq/README.md) | `banyan-mq` in action: producers and consumers on Kafka and RocketMQ, multi-backend routing, payload types |
| [`banyan-samples-sign/`](banyan-samples-sign/README.md) | `banyan-sign` in action: signed provider plus HttpClient5 and RestTemplate clients |
| [`banyan-samples-feign/`](banyan-samples-feign/README.md) | `banyan-spring-feign` in action: header forwarding, logging and traceId propagation |
| [`banyan-samples-mvc/`](banyan-samples-mvc/README.md) | `banyan-spring-mvc` in action: ordered exception advices, replaceable error response and optional Jackson enhancements |

## Conventions

- Every module is a standalone Spring Boot app: examples under `src/main/java`, tests under `src/test/java`.
- Most modules default `server.port` to `0` (random); modules that need a stable client URL use a fixed unusual port (18761–18768), overridable with `SERVER_PORT`.
- Broker addresses come from `${ENV_VAR:default}` placeholders in `application.yml`; no local address is hard-coded.
- Tests prefer embedded infrastructure: `EmbeddedKafka` for Kafka, Testcontainers for RocketMQ. No external broker is required.

## Build

Samples depend on `cn.kennylee.banyan:banyan-mq-*` and `banyan-sign-*` / `banyan-spring-*:0.1.0-SNAPSHOT`. Install the library first in the [banyan](https://github.com/kennylee26/banyan) repo:

```bash
# sign samples (run full install from the banyan repo root)
mvn clean install -DskipTests

# mq samples
mvn -pl banyan-mq/banyan-mq-core,banyan-mq/banyan-mq-commons-spring,banyan-mq/banyan-mq-producer,banyan-mq/banyan-mq-consumer,banyan-mq/banyan-mq-kafka-spring,banyan-mq/banyan-mq-rocketmq-apache install -DskipTests
```

Then, in this repo:

```bash
mvn clean test
```

Each module README explains how to run the app on its own and indexes every test case.
