package com.library.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 通用错误码
    SUCCESS(0, "ok"),
    SYSTEM_ERROR(500, "系统内部错误"),
    PARAM_ERROR(400, "参数错误"),

    // 认证相关 1xxx
    NOT_LOGIN(1001, "未登录或Token失效"),
    TOKEN_EXPIRED(1002, "Token已过期"),
    TOKEN_INVALID(1003, "Token无效"),
    LOGIN_FAILED(1004, "用户名或密码错误"),
    ACCOUNT_DISABLED(1005, "账户已被禁用"),

    // 业务错误 2xxx
    STOCK_NOT_ENOUGH(2001, "库存不足"),
    BOOK_NOT_FOUND(2002, "图书不存在"),
    ALREADY_BORROWED(2003, "该书已被借阅"),
    BORROW_LIMIT_EXCEEDED(2004, "超出借阅数量限制"),
    RECORD_NOT_FOUND(2005, "借阅记录不存在"),
    ALREADY_RETURNED(2006, "该书已归还"),
    CATEGORY_NOT_FOUND(2007, "分类不存在"),
    USER_NOT_FOUND(2008, "用户不存在"),
    USERNAME_EXISTS(2009, "用户名已存在"),
    ISBN_EXISTS(2010, "ISBN已存在"),
    CATEGORY_EXISTS(2011, "分类名称已存在"),
    CATEGORY_HAS_BOOKS(2012, "该分类下存在图书，无法删除"),
    CATEGORY_HAS_CHILDREN(2013, "该分类下存在子分类，无法删除"),

    // 权限相关 3xxx
    NO_PERMISSION(3001, "无权限访问"),
    ADMIN_REQUIRED(3002, "需要管理员权限");

    private final int code;
    private final String message;
}
