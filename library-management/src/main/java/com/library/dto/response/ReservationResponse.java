package com.library.dto.response;

import com.library.entity.Reservation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {

    private Long id;
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;
    private String bookCoverUrl;
    private Long userId;
    private String username;
    private String status;
    private String statusDesc;
    private Integer queuePosition;
    private LocalDateTime notifiedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    public static ReservationResponse fromEntity(Reservation reservation) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .bookId(reservation.getBook().getId())
                .bookTitle(reservation.getBook().getTitle())
                .bookAuthor(reservation.getBook().getAuthor())
                .bookCoverUrl(reservation.getBook().getCoverUrl())
                .userId(reservation.getUser().getId())
                .username(reservation.getUser().getUsername())
                .status(reservation.getStatus().name())
                .statusDesc(reservation.getStatusDesc())
                .queuePosition(reservation.getQueuePosition())
                .notifiedAt(reservation.getNotifiedAt())
                .expiresAt(reservation.getExpiresAt())
                .createdAt(reservation.getCreatedAt())
                .build();
    }
}
