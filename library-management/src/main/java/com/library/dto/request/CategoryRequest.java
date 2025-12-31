package com.library.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryRequest {

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称不超过50个字符")
    private String name;

    @Size(max = 255, message = "描述不超过255个字符")
    private String description;

    private Long parentId;

    private Integer sortOrder = 0;
}
