package com.library.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 罚款记录实体
 * 记录每一笔罚款的详细信息
 */
@Entity
@Table(name = "fine_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FineRecord extends BaseEntity {

    /**
     * 用户
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 关联的借阅记录
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrow_id", nullable = false)
    private BorrowRecord borrowRecord;

    /**
     * 罚款金额
     */
    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    /**
     * 逾期天数
     */
    @Column(name = "overdue_days", nullable = false)
    private Integer overdueDays;

    /**
     * 罚款状态
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.UNPAID;

    /**
     * 缴费时间
     */
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    /**
     * 免除时间
     */
    @Column(name = "waived_at")
    private LocalDateTime waivedAt;

    /**
     * 免除原因
     */
    @Column(name = "waive_reason", length = 500)
    private String waiveReason;

    /**
     * 操作人ID（免除操作时记录）
     */
    @Column(name = "operator_id")
    private Long operatorId;

    /**
     * 操作人用户名
     */
    @Column(name = "operator_name", length = 50)
    private String operatorName;

    /**
     * 备注
     */
    @Column(length = 500)
    private String remark;

    /**
     * 罚款状态枚举
     */
    public enum Status {
        UNPAID("未缴"),
        PAID("已缴"),
        WAIVED("已免除");

        private final String description;

        Status(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 标记为已缴费
     */
    public void markAsPaid() {
        this.status = Status.PAID;
        this.paidAt = LocalDateTime.now();
    }

    /**
     * 标记为已免除
     */
    public void markAsWaived(String reason, Long operatorId, String operatorName) {
        this.status = Status.WAIVED;
        this.waivedAt = LocalDateTime.now();
        this.waiveReason = reason;
        this.operatorId = operatorId;
        this.operatorName = operatorName;
    }
}
