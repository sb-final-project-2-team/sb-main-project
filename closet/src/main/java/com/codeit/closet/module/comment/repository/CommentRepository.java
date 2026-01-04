package com.codeit.closet.module.feed.repository;

import com.codeit.closet.module.feed.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * 댓글 Repository
 * 피드 댓글 엔티티의 데이터 접근을 담당
 */
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    /**
     * 삭제되지 않은 댓글 단건 조회
     * 
     * @param commentId 댓글 ID
     * @return 댓글 Optional
     */
    Optional<Comment> findByIdAndDeletedAtIsNull(UUID commentId);

    /**
     * 특정 피드의 댓글 목록 조회 (삭제되지 않은 것만)
     * 
     * @param feedId 피드 ID
     * @param pageable 페이징 정보
     * @return 댓글 페이지
     */
    Page<Comment> findAllByFeedIdAndDeletedAtIsNull(UUID feedId, Pageable pageable);

    /**
     * 특정 사용자가 작성한 댓글 목록 조회
     * 
     * @param authorId 작성자 ID
     * @param pageable 페이징 정보
     * @return 댓글 페이지
     */
    Page<Comment> findAllByAuthorIdAndDeletedAtIsNull(UUID authorId, Pageable pageable);

    /**
     * 댓글 조회 with 작성자 정보 (Fetch Join)
     * N+1 문제 해결
     * 
     * @param commentId 댓글 ID
     * @return 댓글 Optional
     */
    @Query("""
        SELECT c
        FROM Comment c
        JOIN FETCH c.author
        WHERE c.id = :commentId
        AND c.deletedAt IS NULL
    """)
    Optional<Comment> findByIdWithAuthor(@Param("commentId") UUID commentId);

    /**
     * 피드별 댓글 목록 조회 with 작성자 정보 (Fetch Join)
     * 
     * @param feedId 피드 ID
     * @param pageable 페이징 정보
     * @return 댓글 페이지
     */
    @Query("""
        SELECT c
        FROM Comment c
        JOIN FETCH c.author
        WHERE c.feed.id = :feedId
        AND c.deletedAt IS NULL
        ORDER BY c.createdAt ASC
    """)
    Page<Comment> findAllByFeedIdWithAuthor(@Param("feedId") UUID feedId, Pageable pageable);

    /**
     * 특정 피드의 댓글 개수 조회 (삭제되지 않은 것만)
     * 
     * @param feedId 피드 ID
     * @return 댓글 개수
     */
    long countByFeedIdAndDeletedAtIsNull(UUID feedId);

    /**
     * 특정 사용자가 작성한 댓글 개수
     * 
     * @param authorId 작성자 ID
     * @return 댓글 개수
     */
    long countByAuthorIdAndDeletedAtIsNull(UUID authorId);

    /**
     * 댓글 존재 여부 확인 (삭제되지 않은 것만)
     * 
     * @param commentId 댓글 ID
     * @return 존재 여부
     */
    boolean existsByIdAndDeletedAtIsNull(UUID commentId);

    /**
     * 피드에 달린 모든 댓글 삭제
     * 피드 삭제 시 사용
     * 
     * @param feedId 피드 ID
     */
    void deleteAllByFeedId(UUID feedId);

    /**
     * 최근 댓글 조회 (삭제되지 않은 것만)
     * 
     * @param feedId 피드 ID
     * @param pageable 페이징 정보
     * @return 댓글 페이지
     */
    @Query("""
        SELECT c
        FROM Comment c
        JOIN FETCH c.author
        WHERE c.feed.id = :feedId
        AND c.deletedAt IS NULL
        ORDER BY c.createdAt DESC
    """)
    Page<Comment> findRecentCommentsByFeedId(@Param("feedId") UUID feedId, Pageable pageable);
}
