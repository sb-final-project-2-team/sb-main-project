package com.codeit.closet.module.follow.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "follows",
uniqueConstraints = {@UniqueConstraint(
        name = "uk_follow_follower_followee",
        columnNames = {"follower_id", "followee_id"}
)})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "follower_id", nullable = false)
    private UUID followerId; // User 객체로 바꿀지 고민, CHECK 제약은?

    @Column(name = "followee_id", nullable = false)
    private UUID followeeId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

}
