package com.codeit.closet.module.feed.repository;

import com.codeit.closet.module.feed.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * 좋아요 Repository
 * 피드 좋아요 엔티티의 데이터 접근을 담당
 */
public interface LikeRepository extends JpaRepository<Like, UUID> {

    /**
     * 피드와 사용자로 좋아요 조회
     * 좋아요 취소 시 사용
     * 
     * @param feedId 피드 ID
     * @param userId 사용자 ID
     * @return 좋아요 Optional
     */
    Optional<Like> findByFeedIdAndUserId(UUID feedId, UUID userId);

    /**
     * 피드와 사용자로 좋아요 존재 여부 확인
     * 중복 좋아요 방지
     * 
     * @param feedId 피드 ID
     * @param userId 사용자 ID
     * @return 존재 여부
     */
    boolean existsByFeedIdAndUserId(UUID feedId, UUID userId);

    /**
     * 특정 피드의 좋아요 개수 조회
     * 
     * @param feedId 피드 ID
     * @return 좋아요 개수
     */
    long countByFeedId(UUID feedId);

    /**
     * 특정 사용자가 좋아요한 피드 개수
     * 
     * @param userId 사용자 ID
     * @return 좋아요한 피드 개수
     */
    long countByUserId(UUID userId);

    /**
     * 피드에 달린 모든 좋아요 삭제
     * 피드 삭제 시 사용 (cascade 대신 명시적 삭제)
     * 
     * @param feedId 피드 ID
     */
    void deleteAllByFeedId(UUID feedId);

    /**
     * 사용자가 특정 피드에 좋아요를 눌렀는지 조회 (Fetch Join)
     * N+1 문제 해결
     * 
     * @param feedId 피드 ID
     * @param userId 사용자 ID
     * @return 좋아요 Optional
     */
    @Query("""
        SELECT l
        FROM Like l
        JOIN FETCH l.feed
        JOIN FETCH l.user
        WHERE l.feed.id = :feedId
        AND l.user.id = :userId
    """)
    Optional<Like> findByFeedIdAndUserIdWithFeedAndUser(
        @Param("feedId") UUID feedId,
        @Param("userId") UUID userId
    );
}
