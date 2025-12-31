package com.library.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "book")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book extends BaseEntity {

    @Column(unique = true, length = 20)
    private String isbn;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 100)
    private String author;

    @Column(length = 100)
    private String publisher;

    @Column(name = "publish_date")
    private LocalDate publishDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "total_count", nullable = false)
    private Integer totalCount = 1;

    @Column(name = "available_count", nullable = false)
    private Integer availableCount = 1;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    @Column(length = 50)
    private String location;

    @Column(nullable = false)
    private Integer status = 1;

    @OneToMany(mappedBy = "book")
    @Builder.Default
    private List<BorrowRecord> borrowRecords = new ArrayList<>();

    public boolean isAvailable() {
        return status == 1 && availableCount > 0;
    }
}
