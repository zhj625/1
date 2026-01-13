package com.library.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 罚款规则请求DTO
 */
@Data
public class FineRuleRequest {

    /**
     * 每日罚金（元/天）
     */
    @NotNull(message = "每日罚金不能为空")
    @DecimalMin(value = "0.01", message = "每日罚金最小为0.01元")
    @DecimalMax(value = "10.00", message = "每日罚金最大为10元")
    private BigDecimal dailyAmount;

    /**
     * 封顶金额（元），0表示不封顶
     */
    @NotNull(message = "封顶金额不能为空")
    @DecimalMin(value = "0", message = "封顶金额不能为负数")
    @DecimalMax(value = "1000.00", message = "封顶金额最大为1000元")
    private BigDecimal maxAmount;

    /**
     * 免罚天数（宽限期）
     */
    @NotNull(message = "免罚天数不能为空")
    @Min(value = 0, message = "免罚天数最小为0")
    @Max(value = 30, message = "免罚天数最大为30天")
    private Integer graceDays;

    /**
     * 规则描述/备注
     */
    @Size(max = 500, message = "描述不能超过500字")
    private String description;
}
