package com.codeit.closet.module.cloth.entity;


import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;


@Entity
@Table(name = "clothes_attributes_values",
        uniqueConstraints = @UniqueConstraint(
        name = "uk_clothes_attr",
        columnNames = {"clothes_id", "clothes_attributes_id"}
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clothes_id", nullable = false)
    private Cloth cloth;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clothes_attributes_id", nullable = false)
    private ClothAttribute clothAttribute;

    @Column(name = "value", nullable = false)
    private String value;

}
