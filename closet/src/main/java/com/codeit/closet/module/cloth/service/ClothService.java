package com.codeit.closet.module.cloth.service;

import com.codeit.closet.module.cloth.dto.ClothCreateRequest;
import com.codeit.closet.module.cloth.dto.ClothDTO;
import com.codeit.closet.module.cloth.dto.ClothDTOCursorResponse;
import com.codeit.closet.module.cloth.dto.ClothUpdateRequest;

import java.util.UUID;

public interface ClothService {

    ClothDTO create(ClothCreateRequest request);

    ClothDTO find(UUID clothId);

    ClothDTO update(UUID clothId, ClothUpdateRequest request);

    void delete(UUID clothId);

    ClothDTOCursorResponse findAll(UUID ownerId,
                                     String cursor,
                                     UUID idAfter,
                                     Integer limit,
                                     String sortBy,
                                     String sortDirection);
}
