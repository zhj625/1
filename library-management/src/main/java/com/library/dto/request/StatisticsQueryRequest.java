package com.library.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Schema(description = "统计查询请求")
public class StatisticsQueryRequest {

    @Schema(description = "开始日期", example = "2024-01-01")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Schema(description = "结束日期", example = "2024-12-31")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Schema(description = "分类ID（可选，用于筛选特定分类）")
    private Long categoryId;

    @Schema(description = "用户角色（可选，USER/ADMIN/LIBRARIAN）")
    private String userRole;

    @Schema(description = "统计维度：category/month/userRole/all", example = "all")
    private String dimension = "all";

    @Schema(description = "返回记录数限制", example = "10")
    @Min(value = 1, message = "限制数量至少为1")
    private Integer limit = 10;
}
