package com.library.repository;

import com.library.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    @Query("SELECT f FROM Favorite f JOIN FETCH f.book WHERE f.user.id = :userId")
    Page<Favorite> findByUserIdWithBook(@Param("userId") Long userId, Pageable pageable);

    Optional<Favorite> findByUserIdAndBookId(Long userId, Long bookId);

    boolean existsByUserIdAndBookId(Long userId, Long bookId);

    long countByUserId(Long userId);

    long countByBookId(Long bookId);

    void deleteByUserIdAndBookId(Long userId, Long bookId);
}
