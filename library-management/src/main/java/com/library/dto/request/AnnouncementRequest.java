package com.library.dto.request;

import com.library.entity.Announcement;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "公告请求")
public class AnnouncementRequest {

    @Schema(description = "公告标题", example = "图书馆闭馆通知")
    @NotBlank(message = "公告标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200个字符")
    private String title;

    @Schema(description = "公告内容", example = "因系统维护，图书馆将于本周六闭馆一天。")
    private String content;

    @Schema(description = "公告类型：NORMAL-普通, IMPORTANT-重要, ACTIVITY-活动, MAINTENANCE-维护")
    private Announcement.Type type = Announcement.Type.NORMAL;

    @Schema(description = "是否置顶")
    private Boolean pinned = false;

    @Schema(description = "状态：0-草稿, 1-已发布")
    private Integer status = 0;
}
