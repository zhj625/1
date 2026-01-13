package com.library.repository;

import com.library.entity.OperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {

    /**
     * 分页查询操作日志
     */
    @Query("SELECT o FROM OperationLog o WHERE " +
           "(:module IS NULL OR :module = '' OR o.module = :module) AND " +
           "(:operator IS NULL OR :operator = '' OR o.operator LIKE %:operator%) AND " +
           "(:startTime IS NULL OR o.operationTime >= :startTime) AND " +
           "(:endTime IS NULL OR o.operationTime <= :endTime)")
    Page<OperationLog> findByConditions(
            @Param("module") String module,
            @Param("operator") String operator,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable
    );

    /**
     * 统计某个用户的操作次数
     */
    long countByOperator(String operator);

    /**
     * 删除指定时间之前的日志（用于定期清理）
     */
    void deleteByOperationTimeBefore(LocalDateTime time);
}
