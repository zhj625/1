package com.library.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "borrow_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "borrow_date", nullable = false)
    private LocalDateTime borrowDate;

    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    @Column(name = "return_date")
    private LocalDateTime returnDate;

    @Column(nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private Status status = Status.BORROWING;

    @Column(length = 255)
    private String remark;

    /**
     * 续借次数（最多续借2次）
     */
    @Column(name = "renew_count", nullable = false)
    @Builder.Default
    private Integer renewCount = 0;

    /**
     * 逾期天数
     */
    @Column(name = "overdue_days")
    @Builder.Default
    private Integer overdueDays = 0;

    /**
     * 逾期罚款金额（每天0.5元）
     */
    @Column(name = "fine_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal fineAmount = BigDecimal.ZERO;

    /**
     * 罚款是否已缴纳
     */
    @Column(name = "fine_paid", nullable = false)
    @Builder.Default
    private Boolean finePaid = false;

    public enum Status {
        BORROWING(0),   // 借阅中
        RETURNED(1),    // 已归还
        OVERDUE(2);     // 逾期

        private final int value;

        Status(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public boolean isOverdue() {
        return status != Status.RETURNED && LocalDateTime.now().isAfter(dueDate);
    }

    /**
     * 计算逾期天数
     */
    public int calculateOverdueDays() {
        if (!isOverdue()) {
            return 0;
        }
        LocalDateTime endDate = returnDate != null ? returnDate : LocalDateTime.now();
        return (int) ChronoUnit.DAYS.between(dueDate, endDate);
    }

    /**
     * 计算罚款金额（每天0.5元）
     */
    public BigDecimal calculateFine() {
        int days = calculateOverdueDays();
        if (days <= 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal("0.50").multiply(new BigDecimal(days));
    }
}
