package com.codeit.closet.module.clothes.service;

import com.codeit.closet.module.clothes.dto.ClothesCreateRequest;
import com.codeit.closet.module.clothes.dto.ClothesDto;
import com.codeit.closet.module.clothes.dto.ClothesDtoCursorResponse;
import com.codeit.closet.module.clothes.dto.ClothesUpdateRequest;

import java.util.UUID;

public interface ClothesService {

    ClothesDto create(ClothesCreateRequest request);

    ClothesDto find(UUID clothesId);

    ClothesDto update(UUID clothesId, ClothesUpdateRequest request);

    void delete(UUID clothesId);

    ClothesDtoCursorResponse findAll(UUID ownerId,
                                     String cursor,
                                     UUID idAfter,
                                     Integer limit,
                                     String sortBy,
                                     String sortDirection);
}
