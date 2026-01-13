package com.library.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 用于标记需要记录操作日志的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    /**
     * 模块名称
     */
    String module() default "";

    /**
     * 操作类型
     */
    OperationType operation() default OperationType.OTHER;

    /**
     * 操作描述（支持SpEL表达式）
     */
    String description() default "";

    /**
     * 是否记录请求参数
     */
    boolean saveParams() default true;

    /**
     * 是否记录返回结果
     */
    boolean saveResult() default false;

    /**
     * 操作类型枚举
     */
    enum OperationType {
        /**
         * 新增
         */
        CREATE("新增"),
        /**
         * 修改
         */
        UPDATE("修改"),
        /**
         * 删除
         */
        DELETE("删除"),
        /**
         * 查询
         */
        QUERY("查询"),
        /**
         * 导出
         */
        EXPORT("导出"),
        /**
         * 导入
         */
        IMPORT("导入"),
        /**
         * 登录
         */
        LOGIN("登录"),
        /**
         * 登出
         */
        LOGOUT("登出"),
        /**
         * 授权
         */
        GRANT("授权"),
        /**
         * 其他
         */
        OTHER("其他");

        private final String desc;

        OperationType(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }
}
