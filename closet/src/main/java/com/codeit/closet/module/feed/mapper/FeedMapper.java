package com.codeit.closet.module.feed.mapper;

import com.codeit.closet.module.feed.dto.FeedDTO;
import com.codeit.closet.module.feed.dto.OotdDTO;
import com.codeit.closet.module.feed.entity.Feed;
import com.codeit.closet.module.feed.entity.Ootd;
import com.codeit.closet.module.user.mapper.UserMapper;
import com.codeit.closet.module.weather.mapper.WeatherMapper;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
    componentModel = "spring",
    uses = {
        UserMapper.class,
        WeatherMapper.class,
        FeedHelper.class
    }
)
public interface FeedMapper {

  @Mapping(target = "author", source = "feed.user")
  @Mapping(target = "weather", source = "feed.weather")
  @Mapping(target = "ootds", source = "feed.ootds") //ootds 매핑이 안됨.
  @Mapping(target = "likedByMe", source = "feed.likedByMe", qualifiedByName = "getLikedByMe")
  FeedDTO toDTO(Feed feed);

  @Mapping(target = "author", source = "user")
  @Mapping(target = "weather", source = "weather")
  @Mapping(target = "ootds", source = "ootds") //ootds 매핑이 안됨.
  @Mapping(target = "likedByMe", source = "feed.likedByMe", qualifiedByName = "getLikedByMe")
  List<FeedDTO> toDTOs(List<Feed> feeds);

  // 아직 제대로 사용은 못할꺼임.
  OotdDTO toDTO(Ootd ootd);
}
