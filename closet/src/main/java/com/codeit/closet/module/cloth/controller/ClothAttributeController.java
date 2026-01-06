package com.codeit.closet.module.cloth.controller;

import com.codeit.closet.module.cloth.dto.ClothAttributeCreateRequest;
import com.codeit.closet.module.cloth.dto.ClothAttributeDto;
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
    public ResponseEntity<ClothAttributeDto> createClothesAttribute(
            @RequestBody ClothAttributeCreateRequest request
    ) {
        ClothAttributeDto result = clothAttributeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    // 속성 정의 목록 조회
    @GetMapping
    public ResponseEntity<List<ClothAttributeDto>> getClothesAttributes() {
        List<ClothAttributeDto> result = clothAttributeService.findAll();
        return ResponseEntity.ok(result);
    }

    // 속성 정의 단건 조회
    @GetMapping("/{attributeId}")
    public ResponseEntity<ClothAttributeDto> findClothesAttribute(
            @PathVariable UUID attributeId
    ) {
        ClothAttributeDto result = clothAttributeService.find(attributeId);
        return ResponseEntity.ok(result);
    }

    // 속성 정의 수정 (어드민 전용)
    @PatchMapping("/{attributeId}")
    public ResponseEntity<ClothAttributeDto> updateClothesAttribute(
            @PathVariable UUID attributeId,
            @RequestBody ClothAttributeUpdateRequest request
    ) {
        ClothAttributeDto result = clothAttributeService.update(attributeId, request);
        return ResponseEntity.ok(result);
    }

    // 속성 정의 삭제 (어드민 전용)
    @DeleteMapping("/{attributeId}")
    public ResponseEntity<Void> deleteClothesAttribute(
            @PathVariable UUID attributeId
    ) {
        clothAttributeService.delete(attributeId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
