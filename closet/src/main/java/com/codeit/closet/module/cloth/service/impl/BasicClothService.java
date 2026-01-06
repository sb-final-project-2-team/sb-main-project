package com.codeit.closet.module.cloth.service.impl;

import com.codeit.closet.module.cloth.dto.ClothAttributeValueDto;
import com.codeit.closet.module.cloth.dto.ClothCreateRequest;
import com.codeit.closet.module.cloth.dto.ClothDTO;
import com.codeit.closet.module.cloth.dto.ClothDTOCursorResponse;
import com.codeit.closet.module.cloth.dto.ClothUpdateRequest;
import com.codeit.closet.module.cloth.entity.Cloth;
import com.codeit.closet.module.cloth.entity.ClothAttributeValue;
import com.codeit.closet.module.cloth.entity.ClothType;
import com.codeit.closet.module.cloth.repository.ClothAttributeValueRepository;
import com.codeit.closet.module.cloth.repository.ClothRepository;
import com.codeit.closet.module.cloth.service.ClothService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicClothService implements ClothService {

    private final ClothRepository clothRepository;
    private final ClothAttributeValueRepository clothAttributeValueRepository;

    @Override
    @Transactional
    public ClothDTO create(ClothCreateRequest request) {
        // 중복 검사
        if (clothRepository.existsByOwnerIdAndName(request.ownerId(), request.name())) {
            throw new RuntimeException("이미 존재하는 의상 이름입니다: " + request.name());
        }

        // Cloth Entity 생성 및 저장
        Cloth cloth = Cloth.builder()
                .ownerId(request.ownerId())
                .name(request.name())
                .type(ClothType.valueOf(request.type()))
                .build();

        Cloth saved = clothRepository.save(cloth);

        // 속성 값 저장
        if (request.attributes() != null && !request.attributes().isEmpty()) {
            saveAttributes(saved.getId(), request.attributes());
        }

        // DTO 변환 (속성 값 포함)
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ClothDTO find(UUID clothId) {
        Cloth cloth = clothRepository.findById(clothId)
                .orElseThrow(() -> new RuntimeException("Cloth not found: " + clothId));

        return toDto(cloth);
    }

    @Override
    @Transactional
    public ClothDTO update(UUID clothId, ClothUpdateRequest request) {
        // 조회
        Cloth cloth = clothRepository.findById(clothId)
                .orElseThrow(() -> new RuntimeException("Cloth not found: " + clothId));

        // 기본 정보 수정 (null이 아닌 값만)
        if (request.name() != null) {
            cloth.updateName(request.name());
        }
        if (request.type() != null) {
            cloth.updateType(ClothType.valueOf(request.type()));
        }

        // 속성 값 수정 (기존 삭제 후 재생성)
        if (request.attributes() != null) {
            // 기존 속성 값 삭제
            clothAttributeValueRepository.deleteAllByClothId(clothId);

            // 새 속성 값 저장
            if (!request.attributes().isEmpty()) {
                saveAttributes(clothId, request.attributes());
            }
        }

        // @Transactional과 JPA 더티 체킹으로 자동 저장됨
        return toDto(cloth);
    }

    @Override
    @Transactional
    public void delete(UUID clothId) {
        // 존재 확인
        if (!clothRepository.existsById(clothId)) {
            throw new RuntimeException("Cloth not found: " + clothId);
        }

        // 속성 값 먼저 삭제
        clothAttributeValueRepository.deleteAllByClothId(clothId);

        // Cloth 삭제
        clothRepository.deleteById(clothId);
    }

    @Override
    @Transactional(readOnly = true)
    public ClothDTOCursorResponse findAll(UUID ownerId,
                                            String cursor,
                                            UUID idAfter,
                                            Integer limit,
                                            String sortBy,
                                            String sortDirection) {
        // 일단 간단하게 전체 목록 조회 (페이징은 나중에 구현)
        List<Cloth> clothList = clothRepository.findAllByOwnerId(ownerId);

        List<ClothDTO> dtoList = clothList.stream()
                .map(this::toDto)
                .toList();

        return new ClothDTOCursorResponse(
                dtoList,
                null,  // nextCursor
                null,  // nextIdAfter
                false, // hasNext
                dtoList.size(), // totalCount
                sortBy,
                sortDirection
        );
    }

    // 사용자가 옷 등록시, 선택할때 테이블이 생성
    private void saveAttributes(UUID clothId, List<ClothAttributeValueDto> attributes) {
        for (ClothAttributeValueDto attrDto : attributes) {
            ClothAttributeValue attributeValue = ClothAttributeValue.builder()
                    .clothId(clothId)
                    .clothAttributeId(attrDto.attributeId())
                    .value(attrDto.value())
                    .build();

            clothAttributeValueRepository.save(attributeValue); // 레코드 생성
        }
    }

    // Entity -> DTO 변환 (속성 값 포함)
    private ClothDTO toDto(Cloth cloth) {
        // 속성 값 조회
        List<ClothAttributeValue> attributeValues =
                clothAttributeValueRepository.findAllByClothId(cloth.getId());

        // ClothAttributeValue -> ClothAttributeValueDto 변환
        List<ClothAttributeValueDto> attributeDtos = attributeValues.stream()
                .map(av -> new ClothAttributeValueDto(
                        av.getClothAttributeId(),
                        av.getValue()
                ))
                .toList();

        return new ClothDTO(
                cloth.getId(),
                cloth.getOwnerId(),
                cloth.getName(),
                null,  // imageUrl
                cloth.getType().name(),
                attributeDtos
        );
    }
}
