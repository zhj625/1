package com.library.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * 罚款规则配置实体
 * 系统只保留一条有效的规则配置
 */
@Entity
@Table(name = "fine_rule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FineRule extends BaseEntity {

    /**
     * 每日罚金（元/天）
     */
    @Column(name = "daily_amount", precision = 10, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal dailyAmount = new BigDecimal("0.50");

    /**
     * 封顶金额（元），0表示不封顶
     */
    @Column(name = "max_amount", precision = 10, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal maxAmount = new BigDecimal("100.00");

    /**
     * 免罚天数（宽限期）
     */
    @Column(name = "grace_days", nullable = false)
    @Builder.Default
    private Integer graceDays = 0;

    /**
     * 规则描述/备注
     */
    @Column(length = 500)
    private String description;

    /**
     * 是否启用
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    /**
     * 计算罚款金额
     * @param overdueDays 逾期天数
     * @return 罚款金额
     */
    public BigDecimal calculateFine(int overdueDays) {
        if (overdueDays <= graceDays) {
            return BigDecimal.ZERO;
        }

        int effectiveDays = overdueDays - graceDays;
        BigDecimal fine = dailyAmount.multiply(new BigDecimal(effectiveDays));

        // 如果设置了封顶金额且不为0，则取较小值
        if (maxAmount != null && maxAmount.compareTo(BigDecimal.ZERO) > 0) {
            fine = fine.min(maxAmount);
        }

        return fine;
    }
}
