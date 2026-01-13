package com.library.config;

import com.library.common.Result;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("智慧图书馆管理系统 API")
                        .description("""
                                基于 Spring Boot 3 + Spring Security + JWT 的图书馆管理系统后端接口文档

                                ## 认证说明
                                - 带 🔒 标记的接口需要在请求头中携带 JWT Token
                                - Token 通过 `/api/auth/login` 接口获取
                                - **Swagger UI 使用**: 点击右上角 Authorize 按钮，直接粘贴 token 即可（无需手动加 Bearer 前缀）
                                - **实际请求格式**: `Authorization: Bearer {token}`（Swagger UI 会自动拼接）

                                ## 响应格式
                                所有接口统一返回以下 JSON 结构:
                                ```json
                                {
                                  "code": 200,      // 状态码，200表示成功
                                  "message": "操作成功", // 提示信息
                                  "data": {}        // 返回数据（可选）
                                }
                                ```
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Library Admin")
                                .email("admin@library.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("本地开发服务器")))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("直接粘贴登录接口返回的 token 即可，Swagger UI 会自动添加 Bearer 前缀"))
                        .addSchemas("ErrorResult", createErrorResultSchema())
                        .addResponses("UnauthorizedError", new ApiResponse()
                                .description("未认证 - Token 缺失或无效")
                                .content(new Content().addMediaType("application/json",
                                        new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResult")))))
                        .addResponses("ForbiddenError", new ApiResponse()
                                .description("无权限 - 需要管理员角色")
                                .content(new Content().addMediaType("application/json",
                                        new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResult")))))
                        .addResponses("NotFoundError", new ApiResponse()
                                .description("资源不存在")
                                .content(new Content().addMediaType("application/json",
                                        new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResult")))))
                        .addResponses("BadRequestError", new ApiResponse()
                                .description("请求参数错误")
                                .content(new Content().addMediaType("application/json",
                                        new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResult"))))));
    }

    private Schema<?> createErrorResultSchema() {
        Schema<?> schema = new Schema<>();
        schema.setType("object");
        schema.setDescription("统一错误响应格式");
        schema.setProperties(Map.of(
                "code", new Schema<Integer>().type("integer").description("错误码").example(400),
                "message", new Schema<String>().type("string").description("错误信息").example("请求参数错误"),
                "data", new Schema<>().type("object").description("错误详情（可选）").nullable(true)
        ));
        schema.setRequired(List.of("code", "message"));
        return schema;
    }
}
