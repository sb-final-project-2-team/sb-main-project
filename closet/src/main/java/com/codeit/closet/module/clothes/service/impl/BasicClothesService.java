package com.codeit.closet.module.clothes.service.impl;

import com.codeit.closet.module.clothes.dto.ClothesCreateRequest;
import com.codeit.closet.module.clothes.dto.ClothesDTO;
import com.codeit.closet.module.clothes.dto.ClothesDtoCursorResponse;
import com.codeit.closet.module.clothes.dto.ClothesUpdateRequest;
import com.codeit.closet.module.clothes.entity.Clothes;
import com.codeit.closet.module.clothes.entity.ClothesType;
import com.codeit.closet.module.clothes.repository.ClothesRepository;
import com.codeit.closet.module.clothes.service.ClothesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicClothesService implements ClothesService {

    private final ClothesRepository clothesRepository;

    @Override
    @Transactional
    public ClothesDTO create(ClothesCreateRequest request) {
        // 중복 검사
        if (clothesRepository.existsByOwnerIdAndName(request.ownerId(), request.name())) {
            throw new RuntimeException("이미 존재하는 의상 이름입니다: " + request.name());
        }

        // Entity 생성
        Clothes clothes = Clothes.builder()
                .ownerId(request.ownerId())
                .name(request.name())
                .type(ClothesType.valueOf(request.type()))
                .build();

        // 저장
        Clothes saved = clothesRepository.save(clothes);

        // DTO 변환
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ClothesDTO find(UUID clothesId) {
        Clothes clothes = clothesRepository.findById(clothesId)
                .orElseThrow(() -> new RuntimeException("Clothes not found: " + clothesId));

        return toDto(clothes);
    }

    @Override
    @Transactional
    public ClothesDTO update(UUID clothesId, ClothesUpdateRequest request) {
        // 조회
        Clothes clothes = clothesRepository.findById(clothesId)
                .orElseThrow(() -> new RuntimeException("Clothes not found: " + clothesId));

        // 수정 (null이 아닌 값만)
        if (request.name() != null) {
            clothes.updateName(request.name());
        }
        if (request.type() != null) {
            clothes.updateType(ClothesType.valueOf(request.type()));
        }

        // @Transactional과 JPA 더티 체킹으로 자동 저장됨
        return toDto(clothes);
    }

    @Override
    @Transactional
    public void delete(UUID clothesId) {
        // 존재 확인
        if (!clothesRepository.existsById(clothesId)) {
            throw new RuntimeException("Clothes not found: " + clothesId);
        }

        // 삭제
        clothesRepository.deleteById(clothesId);
    }

    @Override
    @Transactional(readOnly = true)
    public ClothesDtoCursorResponse findAll(UUID ownerId,
                                            String cursor,
                                            UUID idAfter,
                                            Integer limit,
                                            String sortBy,
                                            String sortDirection) {
        // 일단 간단하게 전체 목록 조회 (페이징은 나중에 구현)
        List<Clothes> clothesList = clothesRepository.findAllByOwnerId(ownerId);

        List<ClothesDTO> dtoList = clothesList.stream()
                .map(this::toDto)
                .toList();

        return new ClothesDtoCursorResponse(
                dtoList,
                null,  // nextCursor
                null,  // nextIdAfter
                false, // hasNext
                dtoList.size(), // totalCount
                sortBy,
                sortDirection
        );
    }

    // Entity -> DTO 변환
    private ClothesDTO toDto(Clothes clothes) {
        return new ClothesDTO(
                clothes.getId(),
                clothes.getOwnerId(),
                clothes.getName(),
                clothes.getType().name()
        );
    }
}
