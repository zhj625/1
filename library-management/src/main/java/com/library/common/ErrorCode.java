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
    DATA_NOT_FOUND(404, "数据不存在"),
    DATA_CONFLICT(409, "数据冲突，请刷新后重试"),
    CONCURRENT_UPDATE(410, "数据已被修改，请刷新后重试"),
    DATA_INTEGRITY_ERROR(411, "数据完整性错误"),

    // 认证相关 1xxx
    NOT_LOGIN(1001, "未登录或Token失效"),
    TOKEN_EXPIRED(1002, "Token已过期"),
    TOKEN_INVALID(1003, "Token无效"),
    LOGIN_FAILED(1004, "用户名或密码错误"),
    ACCOUNT_DISABLED(1005, "账户已被禁用"),
    ACCOUNT_LOCKED(1006, "账户已被锁定，请稍后重试"),
    LOGIN_ATTEMPTS_EXCEEDED(1007, "登录失败次数过多，账户已被锁定"),
    ACCOUNT_PENDING(1008, "账户正在审核中，请等待管理员审核通过"),
    ACCOUNT_REJECTED(1009, "账户注册申请已被拒绝"),

    // 业务错误 - 图书相关 20xx
    BOOK_NOT_FOUND(2001, "图书不存在"),
    BOOK_DISABLED(2002, "图书已下架"),
    ISBN_EXISTS(2003, "ISBN已存在"),
    ISBN_INVALID(2004, "ISBN格式不正确"),
    STOCK_NOT_ENOUGH(2005, "库存不足，当前可借数量为0"),
    STOCK_INVALID(2006, "可借数量不能超过总库存"),
    BOOK_HAS_ACTIVE_BORROWS(2007, "图书存在未归还的借阅记录，无法删除"),

    // 业务错误 - 借阅相关 21xx
    BORROW_NOT_FOUND(2101, "借阅记录不存在"),
    ALREADY_BORROWED(2102, "您已借阅该书且尚未归还"),
    ALREADY_RETURNED(2103, "该书已归还，请勿重复操作"),
    BORROW_LIMIT_EXCEEDED(2104, "超出借阅数量限制"),
    BORROW_DAYS_INVALID(2105, "借阅天数必须在1-90天之间"),
    BORROW_RECORD_STATUS_ERROR(2106, "借阅记录状态异常"),
    RETURN_PERMISSION_DENIED(2107, "只能归还本人的借阅记录"),
    RENEW_LIMIT_EXCEEDED(2108, "已达到最大续借次数"),
    RENEW_NOT_ALLOWED(2109, "当前状态不允许续借"),
    RENEW_OVERDUE_NOT_ALLOWED(2110, "逾期图书不能续借，请先归还"),
    FINE_NOT_PAID(2111, "请先缴纳逾期罚款"),
    FINE_ALREADY_PAID(2112, "罚款已缴纳"),

    // 业务错误 - 分类相关 22xx
    CATEGORY_NOT_FOUND(2201, "分类不存在"),
    CATEGORY_EXISTS(2202, "分类名称已存在"),
    CATEGORY_HAS_BOOKS(2203, "该分类下存在图书，无法删除"),
    CATEGORY_HAS_CHILDREN(2204, "该分类下存在子分类，无法删除"),
    CATEGORY_PARENT_INVALID(2205, "不能将分类设为自己的子分类"),
    CATEGORY_PARENT_NOT_FOUND(2206, "父分类不存在"),
    CATEGORY_DEPTH_EXCEEDED(2207, "分类层级不能超过3层"),

    // 业务错误 - 用户相关 23xx
    USER_NOT_FOUND(2301, "用户不存在"),
    USERNAME_EXISTS(2302, "用户名已存在"),
    PASSWORD_INVALID(2303, "密码格式不正确，需6-20位"),
    USER_HAS_ACTIVE_BORROWS(2304, "用户存在未归还的借阅记录，无法删除"),
    EMAIL_EXISTS(2305, "邮箱已被使用"),

    // 业务错误 - 收藏相关 24xx
    FAVORITE_NOT_FOUND(2401, "收藏记录不存在"),
    FAVORITE_EXISTS(2402, "已收藏该图书"),

    // 业务错误 - 预约相关 26xx
    RESERVATION_NOT_FOUND(2601, "预约记录不存在"),
    RESERVATION_EXISTS(2602, "您已预约该图书"),
    RESERVATION_NOT_ALLOWED(2603, "该图书有库存，无需预约，可直接借阅"),
    RESERVATION_CANCEL_NOT_ALLOWED(2604, "当前状态不允许取消预约"),
    RESERVATION_PERMISSION_DENIED(2605, "只能操作本人的预约记录"),
    RESERVATION_EXPIRED(2606, "预约已过期"),
    RESERVATION_PRIORITY_REQUIRED(2607, "您有预约优先权，请使用预约借阅"),

    // 业务错误 - 公告相关 27xx
    ANNOUNCEMENT_NOT_FOUND(2701, "公告不存在"),

    // 业务错误 - 文件相关 25xx
    FILE_UPLOAD_ERROR(2501, "文件上传失败"),
    FILE_TYPE_NOT_ALLOWED(2502, "不支持的文件类型"),
    FILE_SIZE_EXCEEDED(2503, "文件大小超过限制"),
    FILE_NOT_FOUND(2504, "文件不存在"),

    // 兼容旧错误码（保持向后兼容）
    RECORD_NOT_FOUND(2005, "借阅记录不存在"),

    // 权限相关 3xxx
    NO_PERMISSION(3001, "无权限访问"),
    ADMIN_REQUIRED(3002, "需要管理员权限");

    private final int code;
    private final String message;
}
