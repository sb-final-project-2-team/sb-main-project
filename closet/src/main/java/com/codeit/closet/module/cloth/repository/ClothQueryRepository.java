package com.codeit.closet.module.cloth.repository;

import com.codeit.closet.module.cloth.dto.ClothDTOCursorResponse;

import java.util.UUID;

public interface ClothQueryRepository {

    ClothDTOCursorResponse findClothsByCursor(
        UUID ownerId,
        String cursor,
        UUID idAfter,
        Integer limit,
        String sortBy,
        String sortDirection,
        String typeEqual
    );
}
