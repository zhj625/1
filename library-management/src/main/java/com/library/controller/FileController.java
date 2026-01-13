package com.library.controller;

import com.library.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.library.config.OpenApiConfig.SECURITY_SCHEME_NAME;

@Tag(name = "文件管理", description = "文件上传相关操作（需要管理员权限）")
@Slf4j
@RestController
@RequestMapping("/api/files")
@SecurityRequirement(name = SECURITY_SCHEME_NAME)
public class FileController {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.base-url:http://localhost:8080}")
    private String baseUrl;

    @Operation(summary = "上传文件", description = "上传图书封面图片，支持 jpg/png 格式，最大 5MB")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "上传成功",
                    content = @Content(schema = @Schema(example = "{\"code\":200,\"message\":\"操作成功\",\"data\":{\"url\":\"http://localhost:8080/uploads/covers/xxx.jpg\",\"filename\":\"xxx.jpg\"}}"))),
            @ApiResponse(responseCode = "400", description = "文件为空/格式不支持/超过大小限制",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult"))),
            @ApiResponse(responseCode = "401", description = "未认证",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult"))),
            @ApiResponse(responseCode = "403", description = "无权限",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult"))),
            @ApiResponse(responseCode = "500", description = "上传失败",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public Result<Map<String, String>> uploadFile(
            @Parameter(description = "要上传的图片文件（支持 jpg/png，最大 5MB）", required = true)
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(400, "请选择要上传的文件");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error(400, "只支持上传图片文件");
        }

        // 验证文件大小 (最大 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            return Result.error(400, "文件大小不能超过5MB");
        }

        try {
            // 创建上传目录
            Path uploadPath = Paths.get(uploadDir, "covers");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + extension;

            // 保存文件
            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath);

            // 返回访问URL
            String fileUrl = baseUrl + "/uploads/covers/" + newFilename;

            Map<String, String> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("filename", newFilename);

            log.info("File uploaded successfully: {}", fileUrl);
            return Result.success(result);

        } catch (IOException e) {
            log.error("File upload failed", e);
            return Result.error(500, "文件上传失败: " + e.getMessage());
        }
    }
}
