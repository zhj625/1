package com.library.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatisticsResponse {

    // 基础统计
    private long totalBooks;          // 图书总数
    private long totalStock;          // 库存总量
    private long totalUsers;          // 用户总数
    private long totalBorrows;        // 借阅总数
    private long activeBorrows;       // 当前借出
    private long overdueBorrows;      // 逾期数量
    private long todayBorrows;        // 今日借阅
    private long todayReturns;        // 今日归还

    // 图表数据
    private List<Map<String, Object>> borrowTrend;      // 借阅趋势（近7天/30天）
    private List<Map<String, Object>> categoryStats;   // 分类统计
    private List<Map<String, Object>> hotBooks;        // 热门图书
    private List<Map<String, Object>> activeUsers;     // 活跃用户
}
