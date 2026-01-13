package com.library.dto.response;

import com.library.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    private Long id;
    private Long userId;
    private String username;
    private String avatar;
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;
    private String bookCoverUrl;
    private Integer rating;
    private String content;
    private Integer likes;
    private Integer status;
    private String createdAt;

    private static final String PLACEHOLDER_AVATAR = "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png";

    public static ReviewResponse fromEntity(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .username(review.getUser().getUsername())
                .avatar(PLACEHOLDER_AVATAR)
                .bookId(review.getBook().getId())
                .bookTitle(review.getBook().getTitle())
                .bookAuthor(review.getBook().getAuthor())
                .bookCoverUrl(review.getBook().getCoverUrl())
                .rating(review.getRating())
                .content(review.getContent())
                .likes(review.getLikes())
                .status(review.getStatus())
                .createdAt(review.getCreatedAt() != null ?
                    review.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null)
                .build();
    }
}
