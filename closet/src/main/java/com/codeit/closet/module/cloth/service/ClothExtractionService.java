package com.codeit.closet.module.cloth.service;

import com.codeit.closet.module.cloth.dto.ClothDTO;

public interface ClothExtractionService {

    ClothDTO extract(String rawUrl);
}