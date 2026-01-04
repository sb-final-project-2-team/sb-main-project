package com.codeit.closet.module.feed.repository;

import com.codeit.closet.module.feed.entity.Feed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * 피드 Repository
 * 피드 엔티티의 데이터 접근을 담당
 */
public interface FeedRepository extends JpaRepository<Feed, UUID> {    //JpaRepository 상속 : 기본 CRUD 메서드 자동 제공, 페이징, 정렬 기능 기본 제공

    /**
     * 삭제되지 않은 피드 단건 조회
     * 
     * @param feedId 피드 ID
     * @return 피드 Optional
     */
    Optional<Feed> findByIdAndDeletedAtIsNull(UUID feedId);    // Optional을 사용 : 데이터가 없을 수 있음을 명시, NullPointerException 방지

    /**
     * 삭제되지 않은 피드 전체 목록 조회 (페이징)
     * 
     * @param pageable 페이징 정보
     * @return 피드 페이지
     */
    Page<Feed> findAllByDeletedAtIsNull(Pageable pageable);

    /**
     * 작성자별 피드 목록 조회
     * 
     * @param authorId 작성자 ID
     * @param pageable 페이징 정보
     * @return 피드 페이지
     */
    Page<Feed> findAllByAuthorIdAndDeletedAtIsNull(UUID authorId, Pageable pageable);     // Pageable : 페이징 정보를 담는 인터페이스, pageNumber, pageSize, sort 정보 포함

    /**
     * 피드 조회 with 작성자 정보 (Fetch Join)
     * N+1 문제 해결을 위한 즉시 로딩
     * 
     * @param feedId 피드 ID
     * @return 피드 Optional
     */
    @Query("""         // Fetch Join : 한 번의 쿼리로 Feed + Author 동시 조회, N+1 문제 완전 해결!
        SELECT f
        FROM Feed f
        JOIN FETCH f.author
        WHERE f.id = :feedId
        AND f.deletedAt IS NULL
    """)
    Optional<Feed> findByIdWithAuthor(@Param("feedId") UUID feedId);

    /**
     * 피드 조회 with 작성자 + 날씨 정보 (Fetch Join)
     * 
     * @param feedId 피드 ID
     * @return 피드 Optional
     */
    @Query("""
        SELECT f
        FROM Feed f
        JOIN FETCH f.author
        JOIN FETCH f.weather
        WHERE f.id = :feedId
        AND f.deletedAt IS NULL
    """)
    Optional<Feed> findByIdWithAuthorAndWeather(@Param("feedId") UUID feedId);

    /**
     * 피드 조회 with 모든 연관 엔티티 (Fetch Join)
     * 단건 상세 조회 시 사용
     * 
     * @param feedId 피드 ID
     * @return 피드 Optional
     */
    @Query("""
        SELECT DISTINCT f        // DISTINCT: ManyToMany 관계에서 중복 제거 필요
        FROM Feed f
        JOIN FETCH f.author
        JOIN FETCH f.weather
        LEFT JOIN FETCH f.ootds        // LEFT JOIN FETCH: ootds가 없을 수도 있으므로 LEFT JOIN
        WHERE f.id = :feedId
        AND f.deletedAt IS NULL
    """)
    Optional<Feed> findByIdWithAll(@Param("feedId") UUID feedId);

    /**
     * 피드 목록 조회 with 작성자 정보 (Fetch Join)
     * 목록 조회 시 N+1 문제 해결
     * 
     * @param pageable 페이징 정보
     * @return 피드 페이지
     */
    @Query("""
        SELECT f
        FROM Feed f
        JOIN FETCH f.author
        WHERE f.deletedAt IS NULL
    """)
    Page<Feed> findAllWithAuthor(Pageable pageable);

    /**
     * 특정 사용자가 특정 피드를 좋아요 했는지 확인
     * 
     * @param feedId 피드 ID
     * @param userId 사용자 ID
     * @return 좋아요 여부
     */
    @Query("""          // @Query : 메서드 네이밍으로 표현 불가능한 복잡한 쿼리, 여러 테이블 조인 필요
        SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END
        FROM Feed f
        JOIN Like l ON l.feed.id = f.id
        WHERE f.id = :feedId
        AND l.user.id = :userId
        AND f.deletedAt IS NULL
    """)
    boolean existsLikeByFeedIdAndUserId(@Param("feedId") UUID feedId, @Param("userId") UUID userId);

    /**
     * 피드 존재 여부 확인 (삭제되지 않은 것만)
     * 
     * @param feedId 피드 ID
     * @return 존재 여부
     */
    boolean existsByIdAndDeletedAtIsNull(UUID feedId);   // 데이터 존재 확인만 필요할 때, findById()보다 빠름 (데이터 로딩 없이 EXISTS 쿼리)

    /**
     * 작성자별 피드 개수 조회
     * 
     * @param authorId 작성자 ID
     * @return 피드 개수
     */
    long countByAuthorIdAndDeletedAtIsNull(UUID authorId);    // 사용자가 작성한 피드 개수, 통계나 페이징에 사용
}
