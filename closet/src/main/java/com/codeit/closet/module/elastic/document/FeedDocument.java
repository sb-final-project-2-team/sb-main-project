package com.codeit.closet.module.elastic.document;

import com.codeit.closet.module.weather.entity.PrecipitationType;
import com.codeit.closet.module.weather.entity.SkyStatus;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(indexName = "feed", createIndex = false)
public class FeedDocument {

  @Id
  private String id;

  @Field(type = FieldType.Text, analyzer = "korean_analyzer")
  private String content;

  @Field(type = FieldType.Keyword)
  private String authorId;

  @Field(type = FieldType.Keyword)
  private SkyStatus skyStatus;

  @Field(type = FieldType.Keyword)
  private PrecipitationType precipitationType;

  @Field(type = FieldType.Long)
  private Long likeCount;

  @Field(type = FieldType.Date, format = DateFormat.date_time)
  private Instant createdAt;
}
