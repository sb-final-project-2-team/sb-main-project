package com.codeit.closet.module.cloth.service.impl;

import com.codeit.closet.module.cloth.dto.ClothAttributeCreateRequest;
import com.codeit.closet.module.cloth.dto.ClothAttributeDto;
import com.codeit.closet.module.cloth.dto.ClothAttributeUpdateRequest;
import com.codeit.closet.module.cloth.entity.ClothAttribute;
import com.codeit.closet.module.cloth.repository.ClothAttributeRepository;
import com.codeit.closet.module.cloth.service.ClothAttributeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicClothAttributeService implements ClothAttributeService {

    private final ClothAttributeRepository clothAttributeRepository;

    @Override
    @Transactional
    public ClothAttributeDto create(ClothAttributeCreateRequest request) {
        // 중복 검사
        if (clothAttributeRepository.existsByName(request.name())) {
            throw new RuntimeException("이미 존재하는 속성 이름입니다: " + request.name());
        }

        // Entity 생성
        ClothAttribute attribute = ClothAttribute.builder()
                .name(request.name())
                .attributesValues(request.selectableValues())
                .build();

        // 저장
        ClothAttribute saved = clothAttributeRepository.save(attribute);

        // DTO 변환
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ClothAttributeDto find(UUID attributeId) {
        ClothAttribute attribute = clothAttributeRepository.findById(attributeId)
                .orElseThrow(() -> new RuntimeException("ClothAttribute not found: " + attributeId));

        return toDto(attribute);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClothAttributeDto> findAll() {
        List<ClothAttribute> attributes = clothAttributeRepository.findAll();

        return attributes.stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ClothAttributeDto update(UUID attributeId, ClothAttributeUpdateRequest request) {
        // 조회
        ClothAttribute attribute = clothAttributeRepository.findById(attributeId)
                .orElseThrow(() -> new RuntimeException("ClothAttribute not found: " + attributeId));

        // 수정 (null이 아닌 값만)
        if (request.name() != null) {
            attribute.updateName(request.name());
        }
        if (request.selectableValues() != null) {
            attribute.updateAttributesValues(request.selectableValues());
        }

        // @Transactional과 JPA 더티 체킹으로 자동 저장됨
        return toDto(attribute);
    }

    @Override
    @Transactional
    public void delete(UUID attributeId) {
        // 존재 확인
        if (!clothAttributeRepository.existsById(attributeId)) {
            throw new RuntimeException("ClothAttribute not found: " + attributeId);
        }

        // 삭제
        clothAttributeRepository.deleteById(attributeId);
    }

    // Entity -> DTO 변환
    private ClothAttributeDto toDto(ClothAttribute attribute) {
        return new ClothAttributeDto(
                attribute.getId(),
                attribute.getName(),
                attribute.getAttributesValues(),
                attribute.getCreatedAt()
        );
    }
}
