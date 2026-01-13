package com.library.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "review")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    /**
     * 评分 (1-5)
     */
    @Column(nullable = false)
    private Integer rating;

    /**
     * 评论内容
     */
    @Column(nullable = false, length = 1000)
    private String content;

    /**
     * 点赞数
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer likes = 0;

    /**
     * 状态: 0-隐藏, 1-显示
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer status = 1;
}
