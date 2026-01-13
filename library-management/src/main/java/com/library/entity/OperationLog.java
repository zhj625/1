package com.library.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 操作日志实体
 */
@Entity
@Table(name = "operation_log", indexes = {
        @Index(name = "idx_operator", columnList = "operator"),
        @Index(name = "idx_operation_time", columnList = "operation_time"),
        @Index(name = "idx_module", columnList = "module")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 操作模块
     */
    @Column(nullable = false, length = 50)
    private String module;

    /**
     * 操作类型（如：新增、修改、删除、导出、登录等）
     */
    @Column(nullable = false, length = 50)
    private String operation;

    /**
     * 操作描述
     */
    @Column(length = 500)
    private String description;

    /**
     * 请求方法（GET/POST/PUT/DELETE）
     */
    @Column(length = 10)
    private String method;

    /**
     * 请求URL
     */
    @Column(length = 255)
    private String url;

    /**
     * 请求参数（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String params;

    /**
     * 返回结果（JSON格式，可选记录）
     */
    @Column(columnDefinition = "TEXT")
    private String result;

    /**
     * 操作人用户名
     */
    @Column(nullable = false, length = 50)
    private String operator;

    /**
     * 操作人ID
     */
    @Column(name = "operator_id")
    private Long operatorId;

    /**
     * 操作人IP
     */
    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    /**
     * 操作时间
     */
    @Column(name = "operation_time", nullable = false)
    private LocalDateTime operationTime;

    /**
     * 耗时（毫秒）
     */
    @Column(name = "cost_time")
    private Long costTime;

    /**
     * 操作状态（0-失败，1-成功）
     */
    @Column(nullable = false)
    private Integer status;

    /**
     * 错误信息
     */
    @Column(name = "error_msg", length = 2000)
    private String errorMsg;
}
