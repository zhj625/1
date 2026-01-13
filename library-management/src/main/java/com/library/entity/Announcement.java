package com.library.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 公告实体
 */
@Entity
@Table(name = "announcement", indexes = {
        @Index(name = "idx_announcement_status", columnList = "status"),
        @Index(name = "idx_announcement_pinned", columnList = "pinned"),
        @Index(name = "idx_announcement_created", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Announcement extends BaseEntity {

    /**
     * 公告标题
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * 公告内容
     */
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * 公告类型
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private Type type = Type.NORMAL;

    /**
     * 是否置顶
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean pinned = false;

    /**
     * 状态：0-草稿，1-已发布
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer status = 0;

    /**
     * 发布人ID
     */
    @Column(name = "publisher_id")
    private Long publisherId;

    /**
     * 发布人用户名
     */
    @Column(name = "publisher_name", length = 50)
    private String publisherName;

    /**
     * 公告类型枚举
     */
    public enum Type {
        NORMAL,     // 普通公告
        IMPORTANT,  // 重要通知
        ACTIVITY,   // 活动通知
        MAINTENANCE // 维护通知
    }
}
