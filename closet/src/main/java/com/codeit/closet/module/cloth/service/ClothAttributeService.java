package com.codeit.closet.module.cloth.service;

import com.codeit.closet.module.cloth.dto.ClothAttributeCreateRequest;
import com.codeit.closet.module.cloth.dto.ClothAttributeDTO;
import com.codeit.closet.module.cloth.dto.ClothAttributeUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface ClothAttributeService {


    ClothAttributeDTO createClothAttribute(ClothAttributeCreateRequest request);

    ClothAttributeDTO findClothAttribute(UUID attributeId);

    List<ClothAttributeDTO> findAllClothAttributes();

    ClothAttributeDTO updateClothAttribute(UUID attributeId, ClothAttributeUpdateRequest request);

    void deleteClothAttribute(UUID attributeId);
}
