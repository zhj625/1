package com.library.service.impl;

import com.library.common.ErrorCode;
import com.library.common.PageResult;
import com.library.dto.request.AnnouncementRequest;
import com.library.dto.response.AnnouncementResponse;
import com.library.entity.Announcement;
import com.library.entity.User;
import com.library.exception.BusinessException;
import com.library.repository.AnnouncementRepository;
import com.library.service.AnnouncementService;
import com.library.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final UserService userService;

    @Override
    @Transactional
    public AnnouncementResponse createAnnouncement(AnnouncementRequest request) {
        User currentUser = userService.getCurrentUserEntity();

        Announcement announcement = Announcement.builder()
                .title(request.getTitle().trim())
                .content(request.getContent())
                .type(request.getType() != null ? request.getType() : Announcement.Type.NORMAL)
                .pinned(request.getPinned() != null ? request.getPinned() : false)
                .status(request.getStatus() != null ? request.getStatus() : 0)
                .publisherId(currentUser.getId())
                .publisherName(currentUser.getUsername())
                .build();

        announcement = announcementRepository.save(announcement);
        log.info("创建公告成功: id={}, title={}", announcement.getId(), announcement.getTitle());

        return AnnouncementResponse.fromEntity(announcement);
    }

    @Override
    @Transactional
    public AnnouncementResponse updateAnnouncement(Long id, AnnouncementRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "公告ID无效");
        }

        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ANNOUNCEMENT_NOT_FOUND));

        announcement.setTitle(request.getTitle().trim());
        announcement.setContent(request.getContent());
        if (request.getType() != null) {
            announcement.setType(request.getType());
        }
        if (request.getPinned() != null) {
            announcement.setPinned(request.getPinned());
        }
        if (request.getStatus() != null) {
            announcement.setStatus(request.getStatus());
        }

        announcement = announcementRepository.save(announcement);
        log.info("更新公告成功: id={}, title={}", announcement.getId(), announcement.getTitle());

        return AnnouncementResponse.fromEntity(announcement);
    }

    @Override
    @Transactional
    public void deleteAnnouncement(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "公告ID无效");
        }

        if (!announcementRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.ANNOUNCEMENT_NOT_FOUND);
        }

        announcementRepository.deleteById(id);
        log.info("删除公告成功: id={}", id);
    }

    @Override
    public AnnouncementResponse getAnnouncementById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "公告ID无效");
        }

        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ANNOUNCEMENT_NOT_FOUND));

        return AnnouncementResponse.fromEntity(announcement);
    }

    @Override
    public PageResult<AnnouncementResponse> getAnnouncements(Integer status, Announcement.Type type, String keyword, int page, int size) {
        page = page > 0 ? page : 1;
        size = size > 0 ? Math.min(size, 100) : 10;

        PageRequest pageRequest = PageRequest.of(page - 1, size);
        Page<Announcement> announcementPage = announcementRepository.findByConditions(status, type, keyword, pageRequest);

        return PageResult.of(
                announcementPage.getContent().stream()
                        .map(AnnouncementResponse::fromEntity)
                        .collect(Collectors.toList()),
                announcementPage.getTotalElements(),
                page,
                size
        );
    }

    @Override
    public PageResult<AnnouncementResponse> getPublishedAnnouncements(int page, int size) {
        page = page > 0 ? page : 1;
        size = size > 0 ? Math.min(size, 100) : 10;

        PageRequest pageRequest = PageRequest.of(page - 1, size);
        Page<Announcement> announcementPage = announcementRepository.findPublished(pageRequest);

        return PageResult.of(
                announcementPage.getContent().stream()
                        .map(AnnouncementResponse::fromEntity)
                        .collect(Collectors.toList()),
                announcementPage.getTotalElements(),
                page,
                size
        );
    }

    @Override
    public List<AnnouncementResponse> getLatestAnnouncements(int limit) {
        limit = limit > 0 ? Math.min(limit, 20) : 5;
        PageRequest pageRequest = PageRequest.of(0, limit);
        List<Announcement> announcements = announcementRepository.findLatest(pageRequest);

        return announcements.stream()
                .map(AnnouncementResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AnnouncementResponse publishAnnouncement(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "公告ID无效");
        }

        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ANNOUNCEMENT_NOT_FOUND));

        if (announcement.getStatus() == 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "公告已发布");
        }

        announcement.setStatus(1);
        announcement = announcementRepository.save(announcement);
        log.info("发布公告成功: id={}, title={}", announcement.getId(), announcement.getTitle());

        return AnnouncementResponse.fromEntity(announcement);
    }

    @Override
    @Transactional
    public AnnouncementResponse unpublishAnnouncement(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "公告ID无效");
        }

        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ANNOUNCEMENT_NOT_FOUND));

        if (announcement.getStatus() == 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "公告未发布");
        }

        announcement.setStatus(0);
        announcement = announcementRepository.save(announcement);
        log.info("撤回公告成功: id={}, title={}", announcement.getId(), announcement.getTitle());

        return AnnouncementResponse.fromEntity(announcement);
    }

    @Override
    @Transactional
    public AnnouncementResponse togglePin(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "公告ID无效");
        }

        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ANNOUNCEMENT_NOT_FOUND));

        announcement.setPinned(!announcement.getPinned());
        announcement = announcementRepository.save(announcement);
        log.info("切换公告置顶状态: id={}, pinned={}", announcement.getId(), announcement.getPinned());

        return AnnouncementResponse.fromEntity(announcement);
    }
}
