package com.library.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    // 用户状态常量
    public static final int STATUS_DISABLED = 0;  // 禁用
    public static final int STATUS_ENABLED = 1;   // 启用
    public static final int STATUS_PENDING = 2;   // 待审核

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "real_name", length = 50)
    private String realName;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @Column(nullable = false)
    private Integer status = STATUS_ENABLED;

    /**
     * 用户角色枚举
     * ADMIN: 管理员 - 拥有所有权限
     * LIBRARIAN: 馆员 - 拥有图书管理、借阅管理、统计查看权限，但无用户管理权限
     * USER: 普通用户 - 仅能借阅图书和查看个人信息
     */
    public enum Role {
        ADMIN,      // 管理员
        LIBRARIAN,  // 馆员
        USER        // 普通用户
    }

    public boolean isEnabled() {
        return status == STATUS_ENABLED;
    }

    public boolean isPending() {
        return status == STATUS_PENDING;
    }

    public boolean isDisabled() {
        return status == STATUS_DISABLED;
    }

    public String getStatusText() {
        return switch (status) {
            case STATUS_DISABLED -> "禁用";
            case STATUS_ENABLED -> "正常";
            case STATUS_PENDING -> "待审核";
            default -> "未知";
        };
    }
}
