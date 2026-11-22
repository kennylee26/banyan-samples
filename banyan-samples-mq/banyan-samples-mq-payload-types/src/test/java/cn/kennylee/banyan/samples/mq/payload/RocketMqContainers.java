package cn.kennylee.banyan.samples.mq.payload;

import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.lifecycle.Startable;

/**
 * 本示例自带的 RocketMQ 容器：nameserver + broker，启动后创建指定 topic。
 *
 * @author kennylee
 * @since 0.1.0
 */
final class RocketMqContainers implements Startable {

    private static final String IMAGE = "apache/rocketmq:5.3.1";

    private final Network network = Network.newNetwork();

    private final GenericContainer<?> namesrv;

    private final GenericContainer<?> broker;

    private final String[] topics;

    RocketMqContainers(String... topics) {
        this.topics = topics.clone();
        this.namesrv = new GenericContainer<>(IMAGE)
                .withNetwork(network)
                .withNetworkAliases("namesrv")
                .withCommand("nameserver")
                .withExposedPorts(9876);
        this.broker = new FixedHostPortGenericContainer<>(IMAGE)
                .withNetwork(network)
                .withNetworkAliases("broker")
                .withClasspathResourceMapping("rocketmq/broker.conf",
                        "/home/rocketmq/broker.conf", BindMode.READ_ONLY)
                .withCommand("broker", "-n", "namesrv:9876", "-c", "/home/rocketmq/broker.conf")
                .withFixedExposedPort(10911, 10911)
                .dependsOn(namesrv);
    }

    @Override
    public void start() {
        namesrv.start();
        broker.start();
        awaitBrokerRegistered();
        for (String topic : topics) {
            createTopic(topic);
        }
    }

    @Override
    public void stop() {
        broker.stop();
        namesrv.stop();
        network.close();
    }

    String nameServer() {
        return namesrv.getHost() + ":" + namesrv.getMappedPort(9876);
    }

    private void awaitBrokerRegistered() {
        for (int attempt = 0; attempt < 30; attempt++) {
            try {
                var result = broker.execInContainer("sh", "-c",
                        "cd /home/rocketmq/rocketmq-5.3.1/bin && ./mqadmin clusterList"
                                + " -n namesrv:9876");
                if (result.getExitCode() == 0 && result.getStdout().contains("broker-a")) {
                    return;
                }
            } catch (Exception ignored) {
                // 容器内命令尚未就绪时重试
            }
            sleepOneSecond();
        }
        throw new IllegalStateException("broker 未在超时内注册到 nameserver");
    }

    private void createTopic(String topic) {
        try {
            var result = broker.execInContainer("sh", "-c",
                    "cd /home/rocketmq/rocketmq-5.3.1/bin && ./mqadmin updateTopic"
                            + " -n namesrv:9876 -c DefaultCluster -t " + topic);
            if (result.getExitCode() != 0) {
                throw new IllegalStateException("创建 topic 失败: " + result.getStderr());
            }
        } catch (Exception e) {
            throw new IllegalStateException("创建 topic 失败: " + topic, e);
        }
    }

    private static void sleepOneSecond() {
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
