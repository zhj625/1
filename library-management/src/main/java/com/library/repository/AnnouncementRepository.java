package com.library.repository;

import com.library.entity.Announcement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    /**
     * 按条件分页查询公告
     */
    @Query("SELECT a FROM Announcement a WHERE " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:type IS NULL OR a.type = :type) AND " +
           "(:keyword IS NULL OR :keyword = '' OR a.title LIKE %:keyword% OR a.content LIKE %:keyword%) " +
           "ORDER BY a.pinned DESC, a.createdAt DESC")
    Page<Announcement> findByConditions(
            @Param("status") Integer status,
            @Param("type") Announcement.Type type,
            @Param("keyword") String keyword,
            Pageable pageable);

    /**
     * 获取已发布的公告列表（前台展示用）
     */
    @Query("SELECT a FROM Announcement a WHERE a.status = 1 ORDER BY a.pinned DESC, a.createdAt DESC")
    Page<Announcement> findPublished(Pageable pageable);

    /**
     * 获取置顶公告
     */
    @Query("SELECT a FROM Announcement a WHERE a.status = 1 AND a.pinned = true ORDER BY a.createdAt DESC")
    List<Announcement> findPinnedAnnouncements();

    /**
     * 获取最新公告（限制数量）
     */
    @Query("SELECT a FROM Announcement a WHERE a.status = 1 ORDER BY a.pinned DESC, a.createdAt DESC")
    List<Announcement> findLatest(Pageable pageable);
}
