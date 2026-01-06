package com.codeit.closet.module.cloth.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "cloth_attributes_values",
        uniqueConstraints = @UniqueConstraint(
        name = "uk_cloth_attr",
        columnNames = {"cloth_id", "cloth_attributes_id"}
        ))
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ClothAttributeValue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "cloth_id", nullable = false)
    private UUID clothId;

    @Column(name = "cloth_attributes_id", nullable = false)
    private UUID clothAttributeId;

    @Column(name = "value", nullable = false, length = 255)
    private String value;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;


}
