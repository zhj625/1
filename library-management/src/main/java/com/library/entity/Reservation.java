package com.library.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservation", indexes = {
    @Index(name = "idx_reservation_book_status", columnList = "book_id, status"),
    @Index(name = "idx_reservation_user_status", columnList = "user_id, status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.WAITING;

    /**
     * 队列中的位置（1表示第一位）
     */
    @Column(name = "queue_position", nullable = false)
    private Integer queuePosition;

    /**
     * 通知用户图书可借阅的时间
     */
    @Column(name = "notified_at")
    private LocalDateTime notifiedAt;

    /**
     * 预约过期时间（通知后3天内有效）
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /**
     * 预约状态枚举
     */
    public enum Status {
        /**
         * 等待中 - 用户正在排队等待
         */
        WAITING,

        /**
         * 已通知 - 图书已归还，已通知用户来借阅
         */
        NOTIFIED,

        /**
         * 已完成 - 用户已成功借阅
         */
        FULFILLED,

        /**
         * 已取消 - 用户主动取消预约
         */
        CANCELLED,

        /**
         * 已过期 - 用户未在规定时间内借阅
         */
        EXPIRED
    }

    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        return switch (status) {
            case WAITING -> "排队中";
            case NOTIFIED -> "待借阅";
            case FULFILLED -> "已借阅";
            case CANCELLED -> "已取消";
            case EXPIRED -> "已过期";
        };
    }
}
