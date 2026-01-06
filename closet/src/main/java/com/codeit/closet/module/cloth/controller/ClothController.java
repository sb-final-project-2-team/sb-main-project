package com.codeit.closet.module.cloth.controller;

import com.codeit.closet.module.cloth.dto.ClothCreateRequest;
import com.codeit.closet.module.cloth.dto.ClothDTO;
import com.codeit.closet.module.cloth.dto.ClothDTOCursorResponse;
import com.codeit.closet.module.cloth.dto.ClothUpdateRequest;
import com.codeit.closet.module.cloth.service.ClothService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
            @RequestParam(required = false) UUID ownerId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) UUID idAfter,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection
    ) {
        // ownerId가 없으면 현재 로그인한 사용자의 ID 사용
        if (ownerId == null) {
            ownerId = getCurrentUserId();
        }

        ClothDTOCursorResponse result = clothService.findAll(
                ownerId, cursor, idAfter, limit, sortBy, sortDirection
        );
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    // 현재 로그인한 사용자 ID 가져오기
    private UUID getCurrentUserId() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            // UserDetails에서 userId를 가져오는 방법은 구현에 따라 다를 수 있음
            // 일단 임시로 admin 사용자 ID 반환 (나중에 수정 필요)
            if (userDetails.getUsername().equals("admin@admin.com")) {
                return UUID.fromString("b26af55d-8832-4a8e-ad3d-e89eec484030");
            }
        }
        throw new RuntimeException("인증된 사용자가 없습니다");
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
