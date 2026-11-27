package cn.kennylee.banyan.samples.excel.parser.model;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 上传文件中的订单行。
 *
 * @author kennylee
 * @since 0.1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRow {

    /** 订单号。 */
    @ExcelProperty("订单号")
    private String orderNo;

    /** 手机号。 */
    @ExcelProperty("手机号")
    private String mobile;

    /** 金额。 */
    @ExcelProperty("金额")
    private BigDecimal amount;
}
