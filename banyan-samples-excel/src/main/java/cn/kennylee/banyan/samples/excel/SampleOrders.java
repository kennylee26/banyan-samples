package cn.kennylee.banyan.samples.excel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 样例数据工厂，供测试与启动演示复用。
 *
 * @author kennylee
 * @since 0.1.0
 */
public final class SampleOrders {

    private SampleOrders() {
    }

    /**
     * 生成示例订单。
     *
     * @param count 数量
     * @return 示例订单
     */
    public static List<OrderRow> of(int count) {
        List<OrderRow> rows = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            rows.add(new OrderRow(
                String.format(Locale.ROOT, "ORD-%05d", i + 1),
                "客户" + (i % 10),
                BigDecimal.valueOf(100L + i),
                i % 2 == 0 ? "PAID" : "PENDING"));
        }
        return rows;
    }
}
