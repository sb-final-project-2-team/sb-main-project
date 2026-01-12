package com.codeit.closet.module.feed.dto;

import com.codeit.closet.module.cloth.dto.ClothAttributeValueDTO;
import java.util.List;
import java.util.UUID;

public record OotdDTO(
    UUID clothesId,
    String name,
    String imageUrl,
    String type ,
    List<ClothAttributeValueDTO> attributes
) {

}
