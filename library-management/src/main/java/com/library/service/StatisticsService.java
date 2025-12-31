package com.library.service;

import com.library.dto.response.StatisticsResponse;

public interface StatisticsService {

    StatisticsResponse getStatistics();

    StatisticsResponse getDashboardData();
}
