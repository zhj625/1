package com.library.dto.response;

import com.library.entity.Announcement;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "公告响应")
public class AnnouncementResponse {

    @Schema(description = "公告ID")
    private Long id;

    @Schema(description = "公告标题")
    private String title;

    @Schema(description = "公告内容")
    private String content;

    @Schema(description = "公告类型")
    private String type;

    @Schema(description = "公告类型描述")
    private String typeDesc;

    @Schema(description = "是否置顶")
    private Boolean pinned;

    @Schema(description = "状态：0-草稿, 1-已发布")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "发布人ID")
    private Long publisherId;

    @Schema(description = "发布人用户名")
    private String publisherName;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    public static AnnouncementResponse fromEntity(Announcement announcement) {
        if (announcement == null) {
            return null;
        }

        String typeDesc;
        switch (announcement.getType()) {
            case IMPORTANT:
                typeDesc = "重要通知";
                break;
            case ACTIVITY:
                typeDesc = "活动通知";
                break;
            case MAINTENANCE:
                typeDesc = "维护通知";
                break;
            default:
                typeDesc = "普通公告";
        }

        return AnnouncementResponse.builder()
                .id(announcement.getId())
                .title(announcement.getTitle())
                .content(announcement.getContent())
                .type(announcement.getType().name())
                .typeDesc(typeDesc)
                .pinned(announcement.getPinned())
                .status(announcement.getStatus())
                .statusDesc(announcement.getStatus() == 1 ? "已发布" : "草稿")
                .publisherId(announcement.getPublisherId())
                .publisherName(announcement.getPublisherName())
                .createdAt(announcement.getCreatedAt())
                .updatedAt(announcement.getUpdatedAt())
                .build();
    }
}
