package com.library.dto.response;

import com.library.entity.FineRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 罚款规则响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FineRuleResponse {

    /**
     * 规则ID
     */
    private Long id;

    /**
     * 每日罚金（元/天）
     */
    private BigDecimal dailyAmount;

    /**
     * 封顶金额（元）
     */
    private BigDecimal maxAmount;

    /**
     * 免罚天数（宽限期）
     */
    private Integer graceDays;

    /**
     * 规则描述
     */
    private String description;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 从实体转换
     */
    public static FineRuleResponse fromEntity(FineRule rule) {
        if (rule == null) {
            return null;
        }
        return FineRuleResponse.builder()
                .id(rule.getId())
                .dailyAmount(rule.getDailyAmount())
                .maxAmount(rule.getMaxAmount())
                .graceDays(rule.getGraceDays())
                .description(rule.getDescription())
                .enabled(rule.getEnabled())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }
}
