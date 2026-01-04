package com.codeit.closet.module.feed.entity;

import com.codeit.closet.module.user.entity.User;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * 피드 좋아요 엔티티
 * 사용자의 피드 좋아요를 나타내는 도메인 모델
 */
@Entity
@Table(
    name = "likes",
    uniqueConstraints = {
        @UniqueConstraint(        // Unique 제약조건의 중요성 -> 한 사용자가 같은 피드에 중복 좋아요 불가, DB 레벨에서 보장 → 안전함!
            name = "uk_likes_feed_user",
            columnNames = {"feed_id", "user_id"}
        )
    },
    indexes = {      // 인덱스를 추가 이유 : 성능 최적화 핵심!
        @Index(name = "idx_likes_feed_id", columnList = "feed_id"),
        @Index(name = "idx_likes_user_id", columnList = "user_id")
    }
)
@EntityListeners(AuditingEntityListener.class)
public class Like {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * JPA 기본 생성자
     */
    protected Like() {
    }

    /**
     * 피드 좋아요 생성 팩토리 메서드
     * 
     * @param feed 좋아요할 피드
     * @param user 좋아요한 사용자
     * @return 생성된 피드 좋아요 엔티티
     */
    public static Like create(Feed feed, User user) {
        Like like = new Like();
        like.feed = feed;
        like.user = user;
        return like;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public Feed getFeed() {
        return feed;
    }

    public User getUser() {
        return user;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
