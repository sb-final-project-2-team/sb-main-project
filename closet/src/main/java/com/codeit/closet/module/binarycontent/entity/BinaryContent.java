package com.codeit.closet.module.binarycontent.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Entity
@Getter
@Builder
@Table(name = "binary_contents")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class BinaryContent {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false)
  private UUID id;

  @Column(name = "file_name", nullable = false)
  private String fileName;

  @Column(name = "file_url", nullable = false, length = 500)
  private String fileUrl;

  @Column(nullable = false)
  private Long size;

  @Column(name = "content_type", nullable = false, length = 50)
  private String contentType;

  @CreatedDate
  @Column(name = "created_at", updatable = false, nullable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private Instant updatedAt;

  @PostPersist
  public void initFileUrl() {
    if (fileUrl == null && id != null) {
      this.fileUrl = "/storage/" + id;
    }
  }
}
