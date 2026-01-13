package com.library.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CategoryRequest {

    @NotBlank(message = "分类名称不能为空")
    @Size(min = 1, max = 50, message = "分类名称长度为1-50个字符")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9_\\-\\s]+$", message = "分类名称只能包含中文、字母、数字、下划线和短横线")
    private String name;

    @Size(max = 255, message = "描述不超过255个字符")
    private String description;

    @Positive(message = "父分类ID必须为正数")
    private Long parentId;

    @Min(value = 0, message = "排序值不能小于0")
    @Max(value = 9999, message = "排序值不能超过9999")
    private Integer sortOrder = 0;
}
