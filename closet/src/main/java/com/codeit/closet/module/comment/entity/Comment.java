package com.codeit.closet.module.feed.entity;

import com.codeit.closet.module.user.entity.User;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * 피드 댓글 엔티티
 * 피드에 달린 댓글을 나타내는 도메인 모델
 */
@Entity
@Table(
    name = "comments",
    indexes = {
        @Index(name = "idx_comments_feed_id", columnList = "feed_id"),
        @Index(name = "idx_comments_author_id", columnList = "author_id"),
        @Index(name = "idx_comments_created_at", columnList = "created_at")
    }
)
@EntityListeners(AuditingEntityListener.class)
public class Comment {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    /**
     * JPA 기본 생성자
     */
    protected Comment() {
    }

    /**
     * 피드 댓글 생성 팩토리 메서드
     * 
     * @param feed 댓글이 달릴 피드
     * @param author 댓글 작성자
     * @param content 댓글 내용
     * @return 생성된 피드 댓글 엔티티
     */
    public static Comment create(Feed feed, User author, String content) {
        Comment comment = new Comment();
        comment.feed = feed;
        comment.author = author;
        comment.content = content;
        return comment;
    }

    /**
     * 댓글 내용 수정
     * 
     * @param content 새로운 댓글 내용
     */
    public void updateContent(String content) {
        this.content = content;
    }

    /**
     * Soft Delete 처리
     */
    public void markAsDeleted() {
        this.deletedAt = Instant.now();
    }

    /**
     * 삭제 여부 확인
     * 
     * @return 삭제된 경우 true
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * 작성자 확인
     * 
     * @param userId 확인할 사용자 ID
     * @return 작성자인 경우 true
     */
    public boolean isAuthor(UUID userId) {
        return this.author.getId().equals(userId);
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public Feed getFeed() {
        return feed;
    }

    public User getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }
}
