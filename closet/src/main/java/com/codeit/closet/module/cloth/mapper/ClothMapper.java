package com.codeit.closet.module.cloth.mapper;

import com.codeit.closet.module.cloth.dto.ClothAttributeValueDTO;
import com.codeit.closet.module.cloth.dto.ClothDTO;
import com.codeit.closet.module.cloth.entity.Cloth;
import com.codeit.closet.module.cloth.entity.ClothAttributeValue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ClothMapper {

    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "imageUrl", source = "binaryContent.fileUrl")
    @Mapping(target = "type", expression = "java(cloth.getType().name())")
    @Mapping(target = "attributes", ignore = true)
    ClothDTO toDTO(Cloth cloth);

    List<ClothDTO> toDTOs(List<Cloth> clothes);

    @Mapping(target = "definitionId", source = "clothAttribute.id")
    @Mapping(target = "value", source = "value")
    ClothAttributeValueDTO toDTO(ClothAttributeValue clothAttributeValue);

    List<ClothAttributeValueDTO> toAttributeDTOs(List<ClothAttributeValue> clothAttributeValues);
}
