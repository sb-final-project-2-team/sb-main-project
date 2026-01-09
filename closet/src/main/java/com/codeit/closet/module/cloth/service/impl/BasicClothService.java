package com.codeit.closet.module.cloth.service.impl;

import com.codeit.closet.module.binarycontent.entity.BinaryContent;
import com.codeit.closet.module.binarycontent.service.BinaryContentService;
import com.codeit.closet.module.cloth.dto.*;
import com.codeit.closet.module.cloth.dto.ClothAttributeValueDTO;
import com.codeit.closet.module.cloth.entity.Cloth;
import com.codeit.closet.module.cloth.entity.ClothAttributeValue;
import com.codeit.closet.module.cloth.entity.ClothType;
import com.codeit.closet.module.cloth.exception.ClothNotFoundException;
import com.codeit.closet.module.cloth.exception.DuplicateClothNameException;
import com.codeit.closet.module.cloth.mapper.ClothMapper;
import com.codeit.closet.module.cloth.repository.ClothAttributeValueRepository;
import com.codeit.closet.module.cloth.repository.ClothQueryRepository;
import com.codeit.closet.module.cloth.repository.ClothRepository;
import com.codeit.closet.module.cloth.service.ClothService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicClothService implements ClothService {

    private final ClothRepository clothRepository;
    private final ClothAttributeValueRepository clothAttributeValueRepository;
    private final BinaryContentService binaryContentService;
    private final ClothMapper clothMapper;
    private final ClothQueryRepository clothQueryRepository;

    @Override
    @Transactional
    public ClothDTO createCloth(ClothCreateRequest request, MultipartFile multipartFile) {
        // 중복 검사
        if (clothRepository.existsByOwnerIdAndName(request.ownerId(), request.name())) {
            throw new DuplicateClothNameException(request.name());
        }

        // 이미지 처리
        BinaryContent binaryContent = null;
        if (multipartFile != null) {
            binaryContent = binaryContentService.createBinaryContent(multipartFile);
        }

        // Cloth Entity 생성 및 저장
        Cloth cloth = Cloth.builder()
                .ownerId(request.ownerId())
                .name(request.name())
                .type(ClothType.valueOf(request.type()))
                .binaryContent(binaryContent)
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
    public ClothDTO findCloth(UUID clothId) {
        Cloth cloth = clothRepository.findById(clothId)
                .orElseThrow(() -> new ClothNotFoundException(clothId));

        return toDto(cloth);
    }

    @Override
    @Transactional
    public ClothDTO updateCloth(UUID clothId, ClothUpdateRequest request, UUID requestUserId, boolean isAdmin, MultipartFile multipartFile) {
        // 조회 및 존재 확인
        Cloth cloth = clothRepository.findById(clothId)
                .orElseThrow(() -> new ClothNotFoundException(clothId));

        // 소유자 또는 관리자만 수정 가능
        if (!cloth.getOwnerId().equals(requestUserId) && !isAdmin) {
            throw new RuntimeException("해당 옷을 수정할 권한이 없습니다");
        }

        // 이미지 처리
        if (multipartFile != null) {
            // 기존 이미지 삭제
            if (cloth.getBinaryContent() != null) {
                binaryContentService.deleteBinaryContent(cloth.getBinaryContent().getId());
            }
            BinaryContent binaryContent = binaryContentService.createBinaryContent(multipartFile);
            cloth.updateBinaryContent(binaryContent);
        }

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
    public void deleteCloth(UUID clothId, UUID requestUserId, boolean isAdmin) {
        // 조회 및 존재 확인
        Cloth cloth = clothRepository.findById(clothId)
                .orElseThrow(() -> new ClothNotFoundException(clothId));

        // 소유자 또는 관리자만 삭제 가능
        if (!cloth.getOwnerId().equals(requestUserId) && !isAdmin) {
            throw new RuntimeException("해당 옷을 삭제할 권한이 없습니다");
        }

        // 속성 값 먼저 삭제
        clothAttributeValueRepository.deleteAllByClothId(clothId);

        // 이미지 삭제
        if (cloth.getBinaryContent() != null) {
            binaryContentService.deleteBinaryContent(cloth.getBinaryContent().getId());
        }

        // Cloth 삭제
        clothRepository.delete(cloth);
    }

    @Override
    @Transactional(readOnly = true)
    public ClothDTOCursorResponse findAllCloths(UUID ownerId,
                                            String cursor,
                                            UUID idAfter,
                                            Integer limit,
                                            String sortBy,
                                            String sortDirection,
                                            String typeEqual) {
        return clothQueryRepository.findClothsByCursor(
                ownerId,
                cursor,
                idAfter,
                limit,
                sortBy,
                sortDirection,
                typeEqual
        );
    }

    // 사용자가 옷 등록시, 선택할때 테이블이 생성
    private void saveAttributes(UUID clothId, List<ClothAttributeValueDTO> attributes) {
        for (ClothAttributeValueDTO attrDto : attributes) {
            ClothAttributeValue attributeValue = ClothAttributeValue.builder()
                    .clothId(clothId)
                    .clothAttributeId(attrDto.definitionId())
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

        // Mapper를 사용하여 변환
        ClothDTO clothDTO = clothMapper.toDTO(cloth);
        List<ClothAttributeValueDTO> attributeDtos = clothMapper.toAttributeDTOs(attributeValues);

        // attributes 필드는 ignore 되어 있으므로 수동으로 설정
        return new ClothDTO(
                clothDTO.id(),
                clothDTO.ownerId(),
                clothDTO.name(),
                clothDTO.imageUrl(),
                clothDTO.type(),
                attributeDtos
        );
    }
}
