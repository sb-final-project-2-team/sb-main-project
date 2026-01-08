package com.codeit.closet.module.feed.dto;

import com.codeit.closet.module.user.dto.user.UserSummary;
import com.codeit.closet.module.weather.dto.weather.WeatherSummaryDTO;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FeedDTO(
    UUID id,
    Instant createdAt,
    Instant updatedAt,
    UserSummary author,
    WeatherSummaryDTO weather,
    List<OotdDTO> ootds,
    String content,
    Long likeCount,
    Integer commentCount,
    Boolean likedByMe
) {
  public FeedDTO withLikedByMe(Boolean likedByMe) {
    return new FeedDTO(
        this.id,
        this.createdAt,
        this.updatedAt,
        this.author,
        this.weather,
        this.ootds,
        this.content,
        this.likeCount,
        this.commentCount,
        likedByMe
    );
  }
}
