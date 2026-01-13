package com.library.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdvancedStatisticsResponse {

    // 时间范围
    private String startDate;
    private String endDate;

    // 汇总数据
    private long totalBorrows;          // 期间总借阅数
    private long totalReturns;          // 期间总归还数
    private long overdueCount;          // 逾期次数
    private BigDecimal totalFines;      // 罚款总额

    // 按分类统计
    private List<CategoryStatItem> categoryStats;

    // 按月份统计
    private List<MonthlyStatItem> monthlyStats;

    // 按用户角色统计
    private List<UserRoleStatItem> userRoleStats;

    // 热门图书（期间内）
    private List<Map<String, Object>> hotBooks;

    // 活跃用户（期间内）
    private List<Map<String, Object>> activeUsers;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryStatItem {
        private String categoryName;
        private long borrowCount;
        private double percentage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthlyStatItem {
        private String month;
        private long borrowCount;
        private long returnCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserRoleStatItem {
        private String role;
        private String roleName;
        private long borrowCount;
        private double percentage;
    }
}
