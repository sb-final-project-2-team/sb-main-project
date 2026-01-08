package com.codeit.closet.module.cloth.service;

import com.codeit.closet.module.cloth.dto.ClothCreateRequest;
import com.codeit.closet.module.cloth.dto.ClothDTO;
import com.codeit.closet.module.cloth.dto.ClothDTOCursorResponse;
import com.codeit.closet.module.cloth.dto.ClothUpdateRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface ClothService {

    ClothDTO createCloth(ClothCreateRequest request, MultipartFile multipartFile);

    ClothDTO findCloth(UUID clothId);

    ClothDTO updateCloth(UUID clothId, ClothUpdateRequest request, UUID requestUserId, boolean isAdmin, MultipartFile multipartFile);

    void deleteCloth(UUID clothId, UUID requestUserId, boolean isAdmin);

    ClothDTOCursorResponse findAllCloths(UUID ownerId,
                                     String cursor,
                                     UUID idAfter,
                                     Integer limit,
                                     String sortBy,
                                     String sortDirection);
}
