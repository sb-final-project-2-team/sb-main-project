package com.codeit.closet.module.cloth.service;

import com.codeit.closet.module.cloth.dto.ClothAttributeCreateRequest;
import com.codeit.closet.module.cloth.dto.ClothAttributeDTO;
import com.codeit.closet.module.cloth.dto.ClothAttributeUpdateRequest;
import com.codeit.closet.module.cloth.entity.ClothAttribute;
import com.codeit.closet.module.cloth.exception.ClothAttributeNotFoundException;
import com.codeit.closet.module.cloth.exception.DuplicateClothAttributeNameException;
import com.codeit.closet.module.cloth.repository.ClothAttributeRepository;
import com.codeit.closet.module.cloth.service.impl.BasicClothAttributeService;
import com.codeit.closet.module.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("BasicClothAttributeService 테스트")
class BasicClothAttributeServiceTest {

    @Mock
    private ClothAttributeRepository clothAttributeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private BasicClothAttributeService clothAttributeService;

    private UUID testAttributeId;
    private ClothAttribute testAttribute;

    @BeforeEach
    void setUp() {
        testAttributeId = UUID.randomUUID();

        // Mock ClothAttribute with ID (lenient)
        testAttribute = mock(ClothAttribute.class);
        lenient().when(testAttribute.getId()).thenReturn(testAttributeId);
        lenient().when(testAttribute.getName()).thenReturn("색상");
        lenient().when(testAttribute.getAttributesValues()).thenReturn(List.of("블랙", "화이트", "블루", "레드"));
        lenient().when(userRepository.findAll()).thenReturn(List.of());
    }

    @Test
    @DisplayName("의상 속성 정의 생성 성공")
    void createClothAttribute_Success() {
        // given
        ClothAttributeCreateRequest request = new ClothAttributeCreateRequest(
                "색상",
                List.of("블랙", "화이트", "블루", "레드")
        );

        when(clothAttributeRepository.existsByName(request.name())).thenReturn(false);
        when(clothAttributeRepository.save(any(ClothAttribute.class))).thenReturn(testAttribute);

        // when
        ClothAttributeDTO result = clothAttributeService.createClothAttribute(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("색상");
        assertThat(result.selectableValues()).hasSize(4);
        assertThat(result.selectableValues()).containsExactly("블랙", "화이트", "블루", "레드");
        verify(clothAttributeRepository, times(1)).save(any(ClothAttribute.class));
        verify(eventPublisher, times(0)).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("의상 속성 정의 생성 실패 - 중복된 이름")
    void createClothAttribute_DuplicateName() {
        // given
        ClothAttributeCreateRequest request = new ClothAttributeCreateRequest(
                "색상",
                List.of("블랙", "화이트")
        );

        when(clothAttributeRepository.existsByName(request.name())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> clothAttributeService.createClothAttribute(request))
                .isInstanceOf(DuplicateClothAttributeNameException.class);

        verify(clothAttributeRepository, never()).save(any());
    }

    @Test
    @DisplayName("의상 속성 정의 조회 성공")
    void findClothAttribute_Success() {
        // given
        when(clothAttributeRepository.findById(testAttributeId)).thenReturn(Optional.of(testAttribute));

        // when
        ClothAttributeDTO result = clothAttributeService.findClothAttribute(testAttributeId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(testAttributeId);
        assertThat(result.name()).isEqualTo("색상");
        assertThat(result.selectableValues()).hasSize(4);
        verify(clothAttributeRepository, times(1)).findById(testAttributeId);
    }

    @Test
    @DisplayName("의상 속성 정의 조회 실패 - 존재하지 않는 속성")
    void findClothAttribute_NotFound() {
        // given
        when(clothAttributeRepository.findById(testAttributeId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> clothAttributeService.findClothAttribute(testAttributeId))
                .isInstanceOf(ClothAttributeNotFoundException.class);

        verify(clothAttributeRepository, times(1)).findById(testAttributeId);
    }

    @Test
    @DisplayName("모든 의상 속성 정의 조회")
    void findAllClothAttributes_Success() {
        // given
        ClothAttribute attribute1 = ClothAttribute.builder()
                .id(UUID.randomUUID())
                .name("색상")
                .attributesValues(List.of("블랙", "화이트"))
                .build();

        ClothAttribute attribute2 = ClothAttribute.builder()
                .id(UUID.randomUUID())
                .name("사이즈")
                .attributesValues(List.of("S", "M", "L"))
                .build();

        when(clothAttributeRepository.findAll()).thenReturn(List.of(attribute1, attribute2));

        // when
        List<ClothAttributeDTO> result = clothAttributeService.findAllClothAttributes();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(ClothAttributeDTO::name)
                .containsExactly("색상", "사이즈");
        verify(clothAttributeRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("모든 의상 속성 정의 조회 - 빈 목록")
    void findAllClothAttributes_EmptyList() {
        // given
        when(clothAttributeRepository.findAll()).thenReturn(List.of());

        // when
        List<ClothAttributeDTO> result = clothAttributeService.findAllClothAttributes();

        // then
        assertThat(result).isEmpty();
        verify(clothAttributeRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("의상 속성 정의 수정 성공 - 이름만 변경")
    void updateClothAttribute_NameOnly() {
        // given
        ClothAttributeUpdateRequest request = new ClothAttributeUpdateRequest(
                "컬러",
                null
        );

        when(clothAttributeRepository.findById(testAttributeId)).thenReturn(Optional.of(testAttribute));

        // when
        ClothAttributeDTO result = clothAttributeService.updateClothAttribute(testAttributeId, request);

        // then
        assertThat(result).isNotNull();
        verify(testAttribute).updateName("컬러");
        verify(testAttribute, never()).updateAttributesValues(any());
    }

    @Test
    @DisplayName("의상 속성 정의 수정 성공 - 값 목록만 변경")
    void updateClothAttribute_ValuesOnly() {
        // given
        List<String> newValues = List.of("블랙", "화이트", "블루", "레드", "그레이");
        ClothAttributeUpdateRequest request = new ClothAttributeUpdateRequest(
                null,
                newValues
        );

        when(clothAttributeRepository.findById(testAttributeId)).thenReturn(Optional.of(testAttribute));

        // when
        ClothAttributeDTO result = clothAttributeService.updateClothAttribute(testAttributeId, request);

        // then
        assertThat(result).isNotNull();
        verify(testAttribute, never()).updateName(any());
        verify(testAttribute).updateAttributesValues(newValues);
    }

    @Test
    @DisplayName("의상 속성 정의 수정 성공 - 이름과 값 모두 변경")
    void updateClothAttribute_NameAndValues() {
        // given
        List<String> newValues = List.of("블랙", "화이트", "블루");
        ClothAttributeUpdateRequest request = new ClothAttributeUpdateRequest(
                "컬러",
                newValues
        );

        when(clothAttributeRepository.findById(testAttributeId)).thenReturn(Optional.of(testAttribute));

        // when
        ClothAttributeDTO result = clothAttributeService.updateClothAttribute(testAttributeId, request);

        // then
        assertThat(result).isNotNull();
        verify(testAttribute).updateName("컬러");
        verify(testAttribute).updateAttributesValues(newValues);
    }

    @Test
    @DisplayName("의상 속성 정의 수정 실패 - 존재하지 않는 속성")
    void updateClothAttribute_NotFound() {
        // given
        ClothAttributeUpdateRequest request = new ClothAttributeUpdateRequest("새 이름", null);
        when(clothAttributeRepository.findById(testAttributeId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> clothAttributeService.updateClothAttribute(testAttributeId, request))
                .isInstanceOf(ClothAttributeNotFoundException.class);
    }

    @Test
    @DisplayName("의상 속성 정의 삭제 성공")
    void deleteClothAttribute_Success() {
        // given
        when(clothAttributeRepository.existsById(testAttributeId)).thenReturn(true);

        // when
        clothAttributeService.deleteClothAttribute(testAttributeId);

        // then
        verify(clothAttributeRepository, times(1)).existsById(testAttributeId);
        verify(clothAttributeRepository, times(1)).deleteById(testAttributeId);
    }

    @Test
    @DisplayName("의상 속성 정의 삭제 실패 - 존재하지 않는 속성")
    void deleteClothAttribute_NotFound() {
        // given
        when(clothAttributeRepository.existsById(testAttributeId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> clothAttributeService.deleteClothAttribute(testAttributeId))
                .isInstanceOf(ClothAttributeNotFoundException.class);

        verify(clothAttributeRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("의상 속성 정의 생성 - 단일 값")
    void createClothAttribute_SingleValue() {
        // given
        ClothAttributeCreateRequest request = new ClothAttributeCreateRequest(
                "특별속성",
                List.of("유일한값")
        );

        ClothAttribute singleValueAttribute = ClothAttribute.builder()
                .id(UUID.randomUUID())
                .name("특별속성")
                .attributesValues(List.of("유일한값"))
                .build();

        when(clothAttributeRepository.existsByName(request.name())).thenReturn(false);
        when(clothAttributeRepository.save(any(ClothAttribute.class))).thenReturn(singleValueAttribute);

        // when
        ClothAttributeDTO result = clothAttributeService.createClothAttribute(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.selectableValues()).hasSize(1);
        assertThat(result.selectableValues()).containsExactly("유일한값");
    }

    @Test
    @DisplayName("의상 속성 정의 생성 - 빈 값 목록")
    void createClothAttribute_EmptyValues() {
        // given
        ClothAttributeCreateRequest request = new ClothAttributeCreateRequest(
                "빈속성",
                List.of()
        );

        ClothAttribute emptyAttribute = ClothAttribute.builder()
                .id(UUID.randomUUID())
                .name("빈속성")
                .attributesValues(List.of())
                .build();

        when(clothAttributeRepository.existsByName(request.name())).thenReturn(false);
        when(clothAttributeRepository.save(any(ClothAttribute.class))).thenReturn(emptyAttribute);

        // when
        ClothAttributeDTO result = clothAttributeService.createClothAttribute(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.selectableValues()).isEmpty();
    }
}
