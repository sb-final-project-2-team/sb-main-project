package com.codeit.closet.module.cloth.controller;

import com.codeit.closet.common.security.ClosetUserDetails;
import com.codeit.closet.module.cloth.dto.ClothCreateRequest;
import com.codeit.closet.module.cloth.dto.ClothDTO;
import com.codeit.closet.module.cloth.dto.ClothDTOCursorResponse;
import com.codeit.closet.module.cloth.dto.ClothUpdateRequest;
import com.codeit.closet.module.cloth.service.ClothExtractionService;
import com.codeit.closet.module.cloth.service.ClothService;
import com.codeit.closet.module.user.entity.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/clothes")
@RequiredArgsConstructor
public class ClothController {

    private final ClothService clothService;
    private final ClothExtractionService clothExtractionService;

    // 옷 등록
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClothDTO> createClothes(
            @RequestPart ClothCreateRequest request,
            @RequestPart(value = "image", required = false) MultipartFile multipartFile
    ) {
        ClothDTO result = clothService.createCloth(request, multipartFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    // 옷 목록 조회
    @GetMapping
    public ResponseEntity<ClothDTOCursorResponse> getClothes(
            @AuthenticationPrincipal ClosetUserDetails userDetails,
            @RequestParam UUID ownerId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) UUID idAfter,
            @RequestParam Integer limit,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection,
            @RequestParam(required = false) String typeEqual
    ) {
        // ownerId가 없으면 현재 로그인한 사용자의 ID 사용
        if (ownerId == null) {
            ownerId = userDetails.getUserDTO().id();
        }

        ClothDTOCursorResponse result = clothService.findAllCloths(
                ownerId, cursor, idAfter, limit, sortBy, sortDirection, typeEqual
        );
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    // 옷 단건 조회
    @GetMapping("/{clothId}")
    public ResponseEntity<ClothDTO> findClothes(
            @PathVariable UUID clothId
    ) {
        ClothDTO result = clothService.findCloth(clothId);
        return ResponseEntity.ok(result);
    }

    // 옷 수정
    @PatchMapping(value = "/{clothId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClothDTO> updateClothes(
            @AuthenticationPrincipal ClosetUserDetails userDetails,
            @PathVariable UUID clothId,
            @RequestPart ClothUpdateRequest request,
            @RequestPart(value = "image", required = false) MultipartFile multipartFile
    ) {
        boolean isAdmin = userDetails.getUserDTO().role() == UserRole.ADMIN;
        ClothDTO result = clothService.updateCloth(clothId, request, userDetails.getUserDTO().id(), isAdmin, multipartFile);
        return ResponseEntity.ok(result);
    }

    // 옷 삭제
    @DeleteMapping("/{clothId}")
    public ResponseEntity<Void> deleteClothes(
            @AuthenticationPrincipal ClosetUserDetails userDetails,
            @PathVariable UUID clothId
    ) {
        boolean isAdmin = userDetails.getUserDTO().role() == UserRole.ADMIN;
        clothService.deleteCloth(clothId, userDetails.getUserDTO().id(), isAdmin);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 구매 링크로 옷 정보 불러오기
    @GetMapping("/extractions")
    public ResponseEntity<ClothDTO> getClothesExtractions(
            @RequestParam("url") String url
    ){
        ClothDTO result = clothExtractionService.extract(url);
        return ResponseEntity.ok(result);
    }
}
