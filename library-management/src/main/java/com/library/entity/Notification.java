package com.library.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 通知类型
     */
    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private Type type;

    /**
     * 通知标题
     */
    @Column(nullable = false, length = 100)
    private String title;

    /**
     * 通知内容
     */
    @Column(nullable = false, length = 500)
    private String content;

    /**
     * 是否已读
     */
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    /**
     * 关联的借阅记录ID（可选）
     */
    @Column(name = "borrow_record_id")
    private Long borrowRecordId;

    public enum Type {
        DUE_REMINDER,       // 即将到期提醒（提前3天）
        OVERDUE_NOTICE,     // 逾期通知
        RETURN_SUCCESS,     // 归还成功
        BORROW_SUCCESS,     // 借阅成功
        FINE_NOTICE,        // 罚款通知
        BOOK_AVAILABLE,     // 预约图书可借阅通知
        RESERVATION_EXPIRED,// 预约过期通知
        SYSTEM              // 系统通知
    }
}
