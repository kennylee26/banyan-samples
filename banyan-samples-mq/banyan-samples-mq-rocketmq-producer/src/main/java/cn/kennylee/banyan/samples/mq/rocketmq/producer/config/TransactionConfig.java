package cn.kennylee.banyan.samples.mq.rocketmq.producer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 事务模板：演示本地事务提交后再发消息。
 *
 * @author kennylee
 * @since 0.1.0
 */
@Configuration(proxyBeanMethods = false)
public class TransactionConfig {

    /**
     * 事务模板。
     *
     * @param transactionManager 事务管理器
     * @return 事务模板
     */
    @Bean
    TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }
}
