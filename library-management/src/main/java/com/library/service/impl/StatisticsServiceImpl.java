package com.library.service.impl;

import com.library.dto.response.StatisticsResponse;
import com.library.entity.BorrowRecord;
import com.library.repository.BookRepository;
import com.library.repository.BorrowRecordRepository;
import com.library.repository.CategoryRepository;
import com.library.repository.UserRepository;
import com.library.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public StatisticsResponse getStatistics() {
        return getDashboardData();
    }

    @Override
    public StatisticsResponse getDashboardData() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        // 基础统计
        long totalBooks = bookRepository.count();
        long totalStock = bookRepository.findAll().stream()
                .mapToLong(book -> book.getTotalCount() != null ? book.getTotalCount() : 0)
                .sum();
        long totalUsers = userRepository.count();
        long totalBorrows = borrowRecordRepository.count();
        long activeBorrows = borrowRecordRepository.countByStatus(BorrowRecord.Status.BORROWING);
        long overdueBorrows = borrowRecordRepository.countByStatus(BorrowRecord.Status.OVERDUE);

        // 今日统计
        long todayBorrows = borrowRecordRepository.countByBorrowDateBetween(todayStart, todayEnd);
        long todayReturns = borrowRecordRepository.countByReturnDateBetween(todayStart, todayEnd);

        // 借阅趋势（近7天）
        List<Map<String, Object>> borrowTrend = getBorrowTrend(7);

        // 分类统计
        List<Map<String, Object>> categoryStats = getCategoryStats();

        // 热门图书（借阅次数最多的10本）
        List<Map<String, Object>> hotBooks = getHotBooks(10);

        // 活跃用户（借阅次数最多的10个用户）
        List<Map<String, Object>> activeUsers = getActiveUsers(10);

        return StatisticsResponse.builder()
                .totalBooks(totalBooks)
                .totalStock(totalStock)
                .totalUsers(totalUsers)
                .totalBorrows(totalBorrows)
                .activeBorrows(activeBorrows)
                .overdueBorrows(overdueBorrows)
                .todayBorrows(todayBorrows)
                .todayReturns(todayReturns)
                .borrowTrend(borrowTrend)
                .categoryStats(categoryStats)
                .hotBooks(hotBooks)
                .activeUsers(activeUsers)
                .build();
    }

    private List<Map<String, Object>> getBorrowTrend(int days) {
        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

            long borrowCount = borrowRecordRepository.countByBorrowDateBetween(startOfDay, endOfDay);
            long returnCount = borrowRecordRepository.countByReturnDateBetween(startOfDay, endOfDay);

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.toString());
            dayData.put("borrows", borrowCount);
            dayData.put("returns", returnCount);
            trend.add(dayData);
        }

        return trend;
    }

    private List<Map<String, Object>> getCategoryStats() {
        List<Map<String, Object>> stats = new ArrayList<>();

        categoryRepository.findAll().forEach(category -> {
            long bookCount = bookRepository.countByCategoryId(category.getId());
            if (bookCount > 0) {
                Map<String, Object> catData = new HashMap<>();
                catData.put("name", category.getName());
                catData.put("value", bookCount);
                stats.add(catData);
            }
        });

        return stats;
    }

    private List<Map<String, Object>> getHotBooks(int limit) {
        List<Object[]> results = borrowRecordRepository.findHotBooks(limit);
        List<Map<String, Object>> hotBooks = new ArrayList<>();

        for (Object[] row : results) {
            Map<String, Object> bookData = new HashMap<>();
            bookData.put("bookId", row[0]);
            bookData.put("title", row[1]);
            bookData.put("author", row[2]);
            bookData.put("borrowCount", row[3]);
            hotBooks.add(bookData);
        }

        return hotBooks;
    }

    private List<Map<String, Object>> getActiveUsers(int limit) {
        List<Object[]> results = borrowRecordRepository.findActiveUsers(limit);
        List<Map<String, Object>> activeUsers = new ArrayList<>();

        for (Object[] row : results) {
            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", row[0]);
            userData.put("username", row[1]);
            userData.put("borrowCount", row[2]);
            activeUsers.add(userData);
        }

        return activeUsers;
    }
}
