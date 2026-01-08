package com.codeit.closet.module.feed.dto;

import java.util.UUID;

public record OotdDTO(
    UUID clothesId,
    String name,
    String imageUrl,
    String type
//    List<ClothesAttributeWithDefDTO> attributes
) {

}
