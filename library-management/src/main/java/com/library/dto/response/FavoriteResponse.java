package com.library.dto.response;

import com.library.entity.Favorite;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteResponse {

    private Long id;
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;
    private String bookCoverUrl;
    private String categoryName;
    private LocalDateTime createdAt;
    private String remark;

    public static FavoriteResponse fromEntity(Favorite favorite) {
        return FavoriteResponse.builder()
                .id(favorite.getId())
                .bookId(favorite.getBook().getId())
                .bookTitle(favorite.getBook().getTitle())
                .bookAuthor(favorite.getBook().getAuthor())
                .bookCoverUrl(favorite.getBook().getCoverUrl())
                .categoryName(favorite.getBook().getCategory() != null ?
                        favorite.getBook().getCategory().getName() : null)
                .createdAt(favorite.getCreatedAt())
                .remark(favorite.getRemark())
                .build();
    }
}
