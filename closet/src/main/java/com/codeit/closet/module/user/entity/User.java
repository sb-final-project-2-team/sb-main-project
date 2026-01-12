package com.codeit.closet.module.user.entity;

import com.codeit.closet.module.binarycontent.entity.BinaryContent;
import com.codeit.closet.module.weather.dto.location.WeatherAPILocation;
import com.codeit.closet.module.weather.entity.WeatherRegion;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

  @OneToOne(orphanRemoval = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @JoinColumn(name = "binary_content_id")
  private BinaryContent binaryContent;

  @OneToOne(orphanRemoval = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @JoinColumn(name = "weather_id")
  private WeatherRegion weather;

  @Column(length = 50, nullable = false)
  private String name;

  @Column(length = 120, nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(length = 20, nullable = false)
  private AuthProvider provider;

  @Column(name = "provider_id", nullable = false)
  private String providerId;

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
  public void initUserDefinition() {
    if (temperatureSensitivity == null) {
      this.temperatureSensitivity = 3;
    }

    if (role == null) {
      this.role = UserRole.USER;
    }

    if (locked == null) {
      this.locked = false;
    }

    if (provider == null) {
      this.provider = AuthProvider.LOCAL;
    }

    if (provider == AuthProvider.LOCAL && providerId == null) {
      this.providerId = this.email;
    }
  }

  public void updateRole(UserRole role) {
    if (role != null) {
      this.role = role;
    }
  }

  public void updateLocked(Boolean locked) {
    if (locked != null) {
      this.locked = locked;
    }
  }

  public void updateSocialInfo(AuthProvider provider, String providerId) {
    if (provider != null && providerId != null) {
      this.provider = provider;
      this.providerId = providerId;
    }
  }

  public void updateTempPassword(String encodedPassword) {
    if (encodedPassword != null) {
      this.tempPassword = encodedPassword;
      this.tempPasswordExpiredAt = OffsetDateTime.now().plusMinutes(3).toInstant();
    }
  }

  public void updateProfile(String name, Instant birthDate, Integer temperatureSensitivity,
      UserGender gender, WeatherRegion weather,BinaryContent binaryContent) {
    if (binaryContent != null) {
      this.binaryContent = binaryContent;
    }

    if (name != null) {
      this.name = name;
    }

    if (birthDate != null) {
      this.birthDate = birthDate;
    }

    if (temperatureSensitivity != null) {
      this.temperatureSensitivity = temperatureSensitivity;
    }

    if (gender != null) {
      this.gender = gender;
    }

    if (weather != null) {
      this. weather = weather;
    }
  }

  public void changePassword(String encodedPassword) {
    this.password = encodedPassword;
  }
}
