package cn.kennylee.banyan.samples.excel;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 导出用订单行，列定义由 {@link ExcelProperty} 决定。
 *
 * @author kennylee
 * @since 0.1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderRow {

    /** 订单号。 */
    @ExcelProperty("订单号")
    private String orderNo;

    /** 客户。 */
    @ExcelProperty("客户")
    private String customer;

    /** 金额。 */
    @ExcelProperty("金额")
    private BigDecimal amount;

    /** 状态。 */
    @ExcelProperty("状态")
    private String status;

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof OrderRow that)) {
            return false;
        }
        return Objects.equals(orderNo, that.orderNo)
            && Objects.equals(customer, that.customer)
            && sameAmount(amount, that.amount)
            && Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderNo, customer, amount == null ? null : amount.stripTrailingZeros(), status);
    }

    private static boolean sameAmount(BigDecimal left, BigDecimal right) {
        if (left == null || right == null) {
            return left == right;
        }
        return left.compareTo(right) == 0;
    }
}
