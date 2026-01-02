package com.codeit.closet.module.clothes.controller;

import com.codeit.closet.module.clothes.dto.ClothesCreateRequest;
import com.codeit.closet.module.clothes.dto.ClothesDto;
import com.codeit.closet.module.clothes.dto.ClothesDtoCursorResponse;
import com.codeit.closet.module.clothes.dto.ClothesUpdateRequest;
import com.codeit.closet.module.clothes.service.ClothesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/clothes")
@RequiredArgsConstructor
public class ClothesController {

    private final ClothesService clothesService;

    // 옷 등록
    @PostMapping
    public ResponseEntity<ClothesDto> createClothes(
            @RequestBody ClothesCreateRequest request
    ) {
        ClothesDto result = clothesService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    // 옷 목록 조회
    @GetMapping
    public ResponseEntity<ClothesDtoCursorResponse> getClothes(
            @RequestParam UUID ownerId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) UUID idAfter,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection
    ) {
        ClothesDtoCursorResponse result = clothesService.findAll(
                ownerId, cursor, idAfter, limit, sortBy, sortDirection
        );
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    // 옷 단건 조회
    @GetMapping("/{clothesId}")
    public ResponseEntity<ClothesDto> findClothes(
            @PathVariable UUID clothesId
    ) {
        ClothesDto result = clothesService.find(clothesId);
        return ResponseEntity.ok(result);
    }

    // 옷 수정
    @PatchMapping("/{clothesId}")
    public ResponseEntity<ClothesDto> updateClothes(
            @PathVariable UUID clothesId,
            @RequestBody ClothesUpdateRequest request
    ) {
        ClothesDto result = clothesService.update(clothesId, request);
        return ResponseEntity.ok(result);
    }

    // 옷 삭제
    @DeleteMapping("/{clothesId}")
    public ResponseEntity<Void> deleteClothes(
            @PathVariable UUID clothesId
    ) {
        clothesService.delete(clothesId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
