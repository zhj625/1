package com.library.service;

import com.library.common.PageResult;
import com.library.dto.request.AnnouncementRequest;
import com.library.dto.response.AnnouncementResponse;
import com.library.entity.Announcement;

import java.util.List;

public interface AnnouncementService {

    /**
     * 创建公告
     */
    AnnouncementResponse createAnnouncement(AnnouncementRequest request);

    /**
     * 更新公告
     */
    AnnouncementResponse updateAnnouncement(Long id, AnnouncementRequest request);

    /**
     * 删除公告
     */
    void deleteAnnouncement(Long id);

    /**
     * 获取公告详情
     */
    AnnouncementResponse getAnnouncementById(Long id);

    /**
     * 分页查询公告（管理端）
     */
    PageResult<AnnouncementResponse> getAnnouncements(Integer status, Announcement.Type type, String keyword, int page, int size);

    /**
     * 获取已发布的公告列表（前台展示）
     */
    PageResult<AnnouncementResponse> getPublishedAnnouncements(int page, int size);

    /**
     * 获取最新公告
     */
    List<AnnouncementResponse> getLatestAnnouncements(int limit);

    /**
     * 发布公告
     */
    AnnouncementResponse publishAnnouncement(Long id);

    /**
     * 撤回公告（改为草稿）
     */
    AnnouncementResponse unpublishAnnouncement(Long id);

    /**
     * 设置/取消置顶
     */
    AnnouncementResponse togglePin(Long id);
}
