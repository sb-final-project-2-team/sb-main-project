package com.codeit.closet.module.cloth.controller;

import com.codeit.closet.common.security.ClosetUserDetails;
import com.codeit.closet.module.cloth.dto.ClothCreateRequest;
import com.codeit.closet.module.cloth.dto.ClothDTO;
import com.codeit.closet.module.cloth.dto.ClothDTOCursorResponse;
import com.codeit.closet.module.cloth.dto.ClothUpdateRequest;
import com.codeit.closet.module.cloth.service.ClothService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/clothes")
@RequiredArgsConstructor
public class ClothController {

    private final ClothService clothService;

    // 옷 등록
    @PostMapping
    public ResponseEntity<ClothDTO> createClothes(
            @RequestBody ClothCreateRequest request
    ) {
        ClothDTO result = clothService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    // 옷 목록 조회
    @GetMapping
    public ResponseEntity<ClothDTOCursorResponse> getClothes(
            @AuthenticationPrincipal ClosetUserDetails userDetails,
            @RequestParam(required = false) UUID ownerId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) UUID idAfter,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection
    ) {
        // ownerId가 없으면 현재 로그인한 사용자의 ID 사용
        if (ownerId == null) {
            ownerId = userDetails.getUserDTO().id();
        }

        ClothDTOCursorResponse result = clothService.findAll(
                ownerId, cursor, idAfter, limit, sortBy, sortDirection
        );
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    // 옷 단건 조회
    @GetMapping("/{clothId}")
    public ResponseEntity<ClothDTO> findClothes(
            @PathVariable UUID clothId
    ) {
        ClothDTO result = clothService.find(clothId);
        return ResponseEntity.ok(result);
    }

    // 옷 수정
    @PatchMapping("/{clothId}")
    public ResponseEntity<ClothDTO> updateClothes(
            @PathVariable UUID clothId,
            @RequestBody ClothUpdateRequest request
    ) {
        ClothDTO result = clothService.update(clothId, request);
        return ResponseEntity.ok(result);
    }

    // 옷 삭제
    @DeleteMapping("/{clothId}")
    public ResponseEntity<Void> deleteClothes(
            @PathVariable UUID clothId
    ) {
        clothService.delete(clothId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
