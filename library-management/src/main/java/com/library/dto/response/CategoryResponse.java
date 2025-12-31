package com.library.dto.response;

import com.library.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    private Long id;
    private String name;
    private String description;
    private Long parentId;
    private String parentName;
    private Integer sortOrder;
    private Integer bookCount;
    private List<CategoryResponse> children;
    private LocalDateTime createdAt;

    public static CategoryResponse fromEntity(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .sortOrder(category.getSortOrder())
                .bookCount(category.getBooks() != null ? category.getBooks().size() : 0)
                .createdAt(category.getCreatedAt())
                .build();
    }

    public static CategoryResponse fromEntityWithChildren(Category category) {
        CategoryResponse response = fromEntity(category);
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            response.setChildren(category.getChildren().stream()
                    .map(CategoryResponse::fromEntityWithChildren)
                    .collect(Collectors.toList()));
        } else {
            response.setChildren(new ArrayList<>());
        }
        return response;
    }
}
