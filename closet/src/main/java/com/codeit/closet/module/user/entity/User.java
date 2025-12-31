package com.codeit.closet.module.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // binaryContent 생성 후 연동할꺼임.
    @Column(name = "binary_content_id")
    private UUID binaryContentId;

    // weather 추가되면 연동할꺼임.
    @Column(name = "weather_id")
    private UUID weatherId;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 120, nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private UserGender gender;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private UserRole role;

    @Column(name = "birth")
    private Instant birthDate;

    @Column(name = "temperature_sensitivity", nullable = false)
    private Integer temperatureSensitivity;

    @Column(name = "temp_password")
    private String tempPassword;

    @Column(name = "temp_password_expired_at")
    private Instant tempPasswordExpiredAt;

    @Column
    private Boolean locked;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    public void initFileUrl() {
        if (temperatureSensitivity == null) {
            this.temperatureSensitivity = 3;
        }
    }
}
