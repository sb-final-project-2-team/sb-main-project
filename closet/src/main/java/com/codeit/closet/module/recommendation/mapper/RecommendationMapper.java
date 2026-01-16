package com.codeit.closet.module.recommendation.mapper;

import com.codeit.closet.module.cloth.dto.ClothAttributeValueDTO;
import com.codeit.closet.module.cloth.entity.Cloth;
import com.codeit.closet.module.recommendation.dto.RecommendationClothDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 추천 도메인 MapStruct 매퍼
 */
@Mapper(componentModel = "spring")
public interface RecommendationMapper {

    /**
     * Cloth 엔티티와 속성 리스트를 RecommendationClothDTO로 변환
     * @param cloth 의상 엔티티
     * @param attributes 의상 속성 리스트
     * @return 추천 의상 DTO
     */
    @Mapping(target = "clothesId", source = "cloth.id")
    @Mapping(target = "name", source = "cloth.name")
    @Mapping(target = "imageUrl", source = "cloth.binaryContent.fileUrl")
    @Mapping(target = "type", expression = "java(cloth.getType().name())")
    @Mapping(target = "attributes", source = "attributes")
    RecommendationClothDTO toRecommendationClothDTO(Cloth cloth, List<ClothAttributeValueDTO> attributes);
}
