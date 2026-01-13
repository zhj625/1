package com.library.repository;

import com.library.entity.FineRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 罚款规则数据访问层
 */
@Repository
public interface FineRuleRepository extends JpaRepository<FineRule, Long> {

    /**
     * 获取当前启用的罚款规则
     */
    @Query("SELECT r FROM FineRule r WHERE r.enabled = true ORDER BY r.updatedAt DESC")
    Optional<FineRule> findActiveRule();

    /**
     * 获取最新的罚款规则
     */
    @Query("SELECT r FROM FineRule r ORDER BY r.id DESC")
    Optional<FineRule> findLatestRule();
}
