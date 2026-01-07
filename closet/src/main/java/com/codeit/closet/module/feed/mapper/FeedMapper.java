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
        WeatherMapper.class
    }
)
public interface FeedMapper {

  @Mapping(target = "author", source = "feed.user")
  @Mapping(target = "weather", source = "feed.weather")
  @Mapping(target = "ootds", source = "feed.ootds")
  @Mapping(target = "likedByMe", ignore = true)
  FeedDTO toDTO(Feed feed);

  List<FeedDTO> toDTOs(List<Feed> feeds);

  @Mapping(target = "clothesId", source = "cloth.id")
  @Mapping(target = "name", source = "cloth.name")
  @Mapping(target = "imageUrl", source = "cloth.binaryContent.fileUrl")
  @Mapping(target = "type", source = "cloth.type")
  OotdDTO toDTO(Ootd ootd);
}
