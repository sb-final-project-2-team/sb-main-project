package com.codeit.closet.module.clothes.entity;


import com.codeit.closet.module.binarycontent.entity.BinaryContent;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "clothes")
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Clothes {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "binary_content_id")
    private BinaryContent binaryContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ClothesType type;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // 비즈니스 메서드
    public void updateName(String name) {
        this.name = name;
    }

    public void updateType(ClothesType type) {
        this.type = type;
    }

}

