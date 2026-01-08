package com.codeit.closet.module.cloth.controller;

import com.codeit.closet.module.cloth.dto.ClothAttributeCreateRequest;
import com.codeit.closet.module.cloth.dto.ClothAttributeDTO;
import com.codeit.closet.module.cloth.dto.ClothAttributeUpdateRequest;
import com.codeit.closet.module.cloth.service.ClothAttributeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clothes/attribute-defs")
@RequiredArgsConstructor
public class ClothAttributeController {

    private final ClothAttributeService clothAttributeService;

    // 속성 정의 생성 (어드민 전용)
    @PostMapping
    public ResponseEntity<ClothAttributeDTO> createClothesAttribute(
            @RequestBody ClothAttributeCreateRequest request
    ) {
        ClothAttributeDTO result = clothAttributeService.createClothAttribute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    // 속성 정의 목록 조회
    @GetMapping
    public ResponseEntity<List<ClothAttributeDTO>> getClothesAttributes() {
        List<ClothAttributeDTO> result = clothAttributeService.findAllClothAttributes();
        return ResponseEntity.ok(result);
    }

    // 속성 정의 단건 조회
    @GetMapping("/{attributeId}")
    public ResponseEntity<ClothAttributeDTO> findClothesAttribute(
            @PathVariable UUID attributeId
    ) {
        ClothAttributeDTO result = clothAttributeService.findClothAttribute(attributeId);
        return ResponseEntity.ok(result);
    }

    // 속성 정의 수정 (어드민 전용)
    @PatchMapping("/{attributeId}")
    public ResponseEntity<ClothAttributeDTO> updateClothesAttribute(
            @PathVariable UUID attributeId,
            @RequestBody ClothAttributeUpdateRequest request
    ) {
        ClothAttributeDTO result = clothAttributeService.updateClothAttribute(attributeId, request);
        return ResponseEntity.ok(result);
    }

    // 속성 정의 삭제 (어드민 전용)
    @DeleteMapping("/{attributeId}")
    public ResponseEntity<Void> deleteClothesAttribute(
            @PathVariable UUID attributeId
    ) {
        clothAttributeService.deleteClothAttribute(attributeId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
