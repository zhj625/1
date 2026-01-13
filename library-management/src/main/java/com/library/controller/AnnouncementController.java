package com.library.controller;

import com.library.annotation.Log;
import com.library.annotation.Log.OperationType;
import com.library.common.PageResult;
import com.library.common.Result;
import com.library.dto.request.AnnouncementRequest;
import com.library.dto.response.AnnouncementResponse;
import com.library.entity.Announcement;
import com.library.service.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.library.config.OpenApiConfig.SECURITY_SCHEME_NAME;

@Tag(name = "公告管理", description = "图书馆公告的增删改查操作")
@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @Operation(summary = "创建公告", description = "创建新公告（需要管理员权限）")
    @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "400", description = "参数校验失败",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @Log(module = "公告管理", operation = OperationType.CREATE, description = "创建公告")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @PostMapping
    public Result<AnnouncementResponse> createAnnouncement(
            @Parameter(description = "公告信息", required = true)
            @Valid @RequestBody AnnouncementRequest request) {
        return Result.success(announcementService.createAnnouncement(request));
    }

    @Operation(summary = "更新公告", description = "根据ID更新公告信息（需要管理员权限）")
    @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "404", description = "公告不存在",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @Log(module = "公告管理", operation = OperationType.UPDATE, description = "修改公告")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @PutMapping("/{id}")
    public Result<AnnouncementResponse> updateAnnouncement(
            @Parameter(description = "公告ID", required = true) @PathVariable Long id,
            @Parameter(description = "公告信息", required = true) @Valid @RequestBody AnnouncementRequest request) {
        return Result.success(announcementService.updateAnnouncement(id, request));
    }

    @Operation(summary = "删除公告", description = "根据ID删除公告（需要管理员权限）")
    @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "404", description = "公告不存在",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @Log(module = "公告管理", operation = OperationType.DELETE, description = "删除公告")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @DeleteMapping("/{id}")
    public Result<Void> deleteAnnouncement(
            @Parameter(description = "公告ID", required = true) @PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return Result.success();
    }

    @Operation(summary = "获取公告详情", description = "根据ID获取公告详情")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "公告不存在",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @GetMapping("/{id}")
    public Result<AnnouncementResponse> getAnnouncementById(
            @Parameter(description = "公告ID", required = true) @PathVariable Long id) {
        return Result.success(announcementService.getAnnouncementById(id));
    }

    @Operation(summary = "分页查询公告（管理端）", description = "根据条件分页查询公告列表（需要管理员权限）")
    @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    @ApiResponse(responseCode = "200", description = "查询成功")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @GetMapping
    public Result<PageResult<AnnouncementResponse>> getAnnouncements(
            @Parameter(description = "状态：0-草稿，1-已发布") @RequestParam(required = false) Integer status,
            @Parameter(description = "公告类型") @RequestParam(required = false) Announcement.Type type,
            @Parameter(description = "关键词搜索") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        return Result.success(announcementService.getAnnouncements(status, type, keyword, page, size));
    }

    @Operation(summary = "获取已发布公告列表（前台）", description = "获取已发布的公告列表，供前台展示")
    @ApiResponse(responseCode = "200", description = "查询成功")
    @GetMapping("/published")
    public Result<PageResult<AnnouncementResponse>> getPublishedAnnouncements(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        return Result.success(announcementService.getPublishedAnnouncements(page, size));
    }

    @Operation(summary = "获取最新公告", description = "获取最新的N条公告，供首页展示")
    @ApiResponse(responseCode = "200", description = "查询成功")
    @GetMapping("/latest")
    public Result<List<AnnouncementResponse>> getLatestAnnouncements(
            @Parameter(description = "返回数量限制") @RequestParam(defaultValue = "5") int limit) {
        return Result.success(announcementService.getLatestAnnouncements(limit));
    }

    @Operation(summary = "发布公告", description = "将草稿状态的公告发布（需要管理员权限）")
    @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "发布成功"),
            @ApiResponse(responseCode = "404", description = "公告不存在",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @Log(module = "公告管理", operation = OperationType.UPDATE, description = "发布公告")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @PostMapping("/{id}/publish")
    public Result<AnnouncementResponse> publishAnnouncement(
            @Parameter(description = "公告ID", required = true) @PathVariable Long id) {
        return Result.success(announcementService.publishAnnouncement(id));
    }

    @Operation(summary = "撤回公告", description = "将已发布的公告撤回为草稿（需要管理员权限）")
    @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "撤回成功"),
            @ApiResponse(responseCode = "404", description = "公告不存在",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @Log(module = "公告管理", operation = OperationType.UPDATE, description = "撤回公告")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @PostMapping("/{id}/unpublish")
    public Result<AnnouncementResponse> unpublishAnnouncement(
            @Parameter(description = "公告ID", required = true) @PathVariable Long id) {
        return Result.success(announcementService.unpublishAnnouncement(id));
    }

    @Operation(summary = "切换置顶状态", description = "设置或取消公告置顶（需要管理员权限）")
    @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "操作成功"),
            @ApiResponse(responseCode = "404", description = "公告不存在",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @Log(module = "公告管理", operation = OperationType.UPDATE, description = "切换公告置顶状态")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @PostMapping("/{id}/toggle-pin")
    public Result<AnnouncementResponse> togglePin(
            @Parameter(description = "公告ID", required = true) @PathVariable Long id) {
        return Result.success(announcementService.togglePin(id));
    }
}
