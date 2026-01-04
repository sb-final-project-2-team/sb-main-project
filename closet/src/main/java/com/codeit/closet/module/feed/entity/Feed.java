package com.codeit.closet.module.feed.entity;

import com.codeit.closet.module.user.entity.User;
import com.codeit.closet.module.weather.entity.Weather;
import com.codeit.closet.module.clothes.entity.Clothes;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 피드 엔티티
 * OOTD 피드를 나타내는 도메인 모델
 */
@Entity      // JPA가 관리하는 엔티티임을 선언
@Table(name = "feeds")       // DB의 feeds 테이블과 매핑
@EntityListeners(AuditingEntityListener.class)         // JPA Auditing 기능 활성화 (생성/수정 시간 자동 관리)
public class Feed {

    @Id
    @UuidGenerator       // Hibernate가 자동으로 UUID 생성
    @Column(name = "id", updatable = false, nullable = false)      //updatable = false : 한번 생성되면 수정 불가
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)       // FetchType.LAZY: 지연 로딩 - Feed 조회 시 User는 실제 사용할 때만 조회
    @JoinColumn(name = "author_id", nullable = false)       // @JoinColumn(name = "author_id"): DB의 author_id 컬럼과 매핑
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weather_id", nullable = false)
    private Weather weather;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "feed_clothes",
        joinColumns = @JoinColumn(name = "feed_id"),
        inverseJoinColumns = @JoinColumn(name = "clothes_id")
    )
    private List<Clothes> ootds = new ArrayList<>();

    @Column(name = "content", columnDefinition = "TEXT")      // columnDefinition = "TEXT": 긴 텍스트 저장 가능 (VARCHAR(255) 제한 해제)
    private String content;

    @Column(name = "like_count", nullable = false)       // 성능 최적화! COUNT 쿼리 매번 실행하지 않아도 됨
    private Long likeCount = 0L;

    @Column(name = "comment_count", nullable = false)
    private Integer commentCount = 0;

    @CreatedDate        // 엔티티 생성 시 자동으로 시간 저장
    @Column(name = "created_at", nullable = false, updatable = false)      // (createdAt만): 생성 시간은 수정 불가
    private Instant createdAt;       // Instant 타입 사용 이유: UTC 기준 타임스탬프, 타임존 이슈 없음

    @LastModifiedDate       // 엔티티 수정 시 자동으로 시간 업데이트
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    /**
     * JPA 기본 생성자
     */
    protected Feed() {
    }

    /**
     * 피드 생성 팩토리 메서드
     * 
     * @param author 작성자
     * @param weather 날씨 정보
     * @param ootds OOTD 의상 목록
     * @param content 피드 내용
     * @return 생성된 피드 엔티티
     */
    //팩토리 매서드 사용 이유 : 의미 있는 이름(Create), 기본값 설정 강제, 유효성 검증 로직 추가 가능, 불변성 보장에 유리
    public static Feed create(User author, Weather weather, List<Clothes> ootds, String content) {
        Feed feed = new Feed();
        feed.author = author;
        feed.weather = weather;
        feed.ootds = ootds != null ? new ArrayList<>(ootds) : new ArrayList<>();
        feed.content = content;
        feed.likeCount = 0L;
        feed.commentCount = 0;
        return feed;
    }

    /**
     * 피드 내용 수정
     * 
     * @param content 새로운 내용
     */
    // setter 대신 비즈니스 메서드 사용 이유 : 의도가 명확함, 비즈니스 규칙 적용 가능(음수 방지), 캡슐화(내부 구현 숨김)
    public void updateContent(String content) {
        this.content = content;
    }

    /**
     * 좋아요 증가
     */
    public void incrementLikeCount() {
        this.likeCount++;
    }

    /**
     * 좋아요 감소
     */
    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    /**
     * 댓글 수 증가
     */
    public void incrementCommentCount() {
        this.commentCount++;
    }

    /**
     * 댓글 수 감소
     */
    public void decrementCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }

    /**
     * Soft Delete 처리
     */
    public void markAsDeleted() {
        this.deletedAt = Instant.now();
    }    // markAsDeleted(): 삭제 시간 기록

    /**
     * 삭제 여부 확인
     * 
     * @return 삭제된 경우 true
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }     // isDeleted(): 삭제 여부 확인

    /**
     * 작성자 확인
     * 
     * @param userId 확인할 사용자 ID
     * @return 작성자인 경우 true
     */
    public boolean isAuthor(UUID userId) {
        return this.author.getId().equals(userId);
    }      // 권한 체크에 사용

    // Getters  -> Setter 안 쓰는 이유 : 불변성 보장, 비즈니스 메서드를 통해서만 상태 변경
    public UUID getId() {
        return id;
    }

    public User getAuthor() {
        return author;
    }

    public Weather getWeather() {
        return weather;
    }

    public List<Clothes> getOotds() {
        return new ArrayList<>(ootds);
    }

    public String getContent() {
        return content;
    }

    public Long getLikeCount() {
        return likeCount;
    }

    public Integer getCommentCount() {
        return commentCount;
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
