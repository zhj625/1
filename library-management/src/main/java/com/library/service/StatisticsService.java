package com.library.service;

import com.library.dto.request.StatisticsQueryRequest;
import com.library.dto.response.AdvancedStatisticsResponse;
import com.library.dto.response.StatisticsResponse;

public interface StatisticsService {

    StatisticsResponse getStatistics();

    StatisticsResponse getDashboardData();

    /**
     * 获取高级统计数据（支持多维度筛选）
     */
    AdvancedStatisticsResponse getAdvancedStatistics(StatisticsQueryRequest request);
}
