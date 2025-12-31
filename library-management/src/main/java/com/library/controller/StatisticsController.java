package com.library.controller;

import com.library.common.Result;
import com.library.dto.response.StatisticsResponse;
import com.library.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<StatisticsResponse> getStatistics() {
        return Result.success(statisticsService.getStatistics());
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<StatisticsResponse> getDashboard() {
        return Result.success(statisticsService.getDashboardData());
    }
}
