package com.codeit.closet.module.cloth.service;

import com.codeit.closet.module.binarycontent.entity.BinaryContent;
import com.codeit.closet.module.binarycontent.service.BinaryContentService;
import com.codeit.closet.module.cloth.dto.ClothAttributeValueDTO;
import com.codeit.closet.module.cloth.dto.ClothCreateRequest;
import com.codeit.closet.module.cloth.dto.ClothDTO;
import com.codeit.closet.module.cloth.dto.ClothUpdateRequest;
import com.codeit.closet.module.cloth.entity.Cloth;
import com.codeit.closet.module.cloth.entity.ClothAttributeValue;
import com.codeit.closet.module.cloth.entity.ClothType;
import com.codeit.closet.module.cloth.exception.ClothNotFoundException;
import com.codeit.closet.module.cloth.exception.DuplicateClothNameException;
import com.codeit.closet.module.cloth.mapper.ClothMapper;
import com.codeit.closet.module.cloth.repository.ClothAttributeValueRepository;
import com.codeit.closet.module.cloth.repository.ClothQueryRepository;
import com.codeit.closet.module.cloth.repository.ClothRepository;
import com.codeit.closet.module.cloth.service.impl.BasicClothService;
import com.codeit.closet.module.user.entity.User;
import com.codeit.closet.module.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BasicClothService 테스트")
class BasicClothServiceTest {

    @Mock
    private ClothRepository clothRepository;

    @Mock
    private ClothAttributeValueRepository clothAttributeValueRepository;

    @Mock
    private BinaryContentService binaryContentService;

    @Mock
    private ClothMapper clothMapper;

    @Mock
    private ClothQueryRepository clothQueryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BasicClothService clothService;

    private UUID testUserId;
    private UUID testClothId;
    private User testUser;
    private Cloth testCloth;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testClothId = UUID.randomUUID();

        // Mock User with ID (lenient)
        testUser = mock(User.class);
        lenient().when(testUser.getId()).thenReturn(testUserId);
        lenient().when(testUser.getName()).thenReturn("testuser");
        lenient().when(testUser.getEmail()).thenReturn("test@example.com");

        // Mock Cloth with ID (lenient)
        testCloth = mock(Cloth.class);
        lenient().when(testCloth.getId()).thenReturn(testClothId);
        lenient().when(testCloth.getName()).thenReturn("블루 데님 자켓");
        lenient().when(testCloth.getType()).thenReturn(ClothType.OUTER);
        lenient().when(testCloth.getOwner()).thenReturn(testUser);
        lenient().when(testCloth.getBinaryContent()).thenReturn(null);
    }

    @Test
    @DisplayName("의상 생성 성공")
    void createCloth_Success() {
        // given
        ClothCreateRequest request = new ClothCreateRequest(
                testUserId,
                "블루 데님 자켓",
                "OUTER",
                new ArrayList<>()
        );

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(clothRepository.existsByOwner_IdAndName(testUserId, request.name())).thenReturn(false);
        when(clothRepository.save(any(Cloth.class))).thenReturn(testCloth);
        when(clothAttributeValueRepository.findAllByCloth_Id(any())).thenReturn(new ArrayList<>());
        when(clothMapper.toDTO(any(Cloth.class))).thenReturn(
                new ClothDTO(testClothId, testUserId, "블루 데님 자켓", null, "OUTER", new ArrayList<>())
        );
        when(clothMapper.toAttributeDTOs(any())).thenReturn(new ArrayList<>());

        // when
        ClothDTO result = clothService.createCloth(request, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("블루 데님 자켓");
        assertThat(result.type()).isEqualTo("OUTER");
        verify(clothRepository, times(1)).save(any(Cloth.class));
    }

    @Test
    @DisplayName("의상 생성 실패 - 사용자가 존재하지 않음")
    void createCloth_UserNotFound() {
        // given
        ClothCreateRequest request = new ClothCreateRequest(
                testUserId,
                "블루 데님 자켓",
                "OUTER",
                new ArrayList<>()
        );

        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> clothService.createCloth(request, null))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("존재하지 않는 사용자입니다");

        verify(clothRepository, never()).save(any());
    }

    @Test
    @DisplayName("의상 생성 실패 - 중복된 이름")
    void createCloth_DuplicateName() {
        // given
        ClothCreateRequest request = new ClothCreateRequest(
                testUserId,
                "블루 데님 자켓",
                "OUTER",
                new ArrayList<>()
        );

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(clothRepository.existsByOwner_IdAndName(testUserId, request.name())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> clothService.createCloth(request, null))
                .isInstanceOf(DuplicateClothNameException.class);

        verify(clothRepository, never()).save(any());
    }

    @Test
    @DisplayName("의상 조회 성공")
    void findCloth_Success() {
        // given
        when(clothRepository.findById(testClothId)).thenReturn(Optional.of(testCloth));
        when(clothAttributeValueRepository.findAllByCloth_Id(testClothId)).thenReturn(new ArrayList<>());
        when(clothMapper.toDTO(testCloth)).thenReturn(
                new ClothDTO(testClothId, testUserId, "블루 데님 자켓", null, "OUTER", new ArrayList<>())
        );
        when(clothMapper.toAttributeDTOs(any())).thenReturn(new ArrayList<>());

        // when
        ClothDTO result = clothService.findCloth(testClothId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("블루 데님 자켓");
        verify(clothRepository, times(1)).findById(testClothId);
    }

    @Test
    @DisplayName("의상 조회 실패 - 존재하지 않는 의상")
    void findCloth_NotFound() {
        // given
        when(clothRepository.findById(testClothId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> clothService.findCloth(testClothId))
                .isInstanceOf(ClothNotFoundException.class);

        verify(clothRepository, times(1)).findById(testClothId);
    }

    @Test
    @DisplayName("의상 수정 성공 - 소유자")
    void updateCloth_SuccessAsOwner() {
        // given
        ClothUpdateRequest request = new ClothUpdateRequest(
                "다크 블루 데님 자켓",
                "OUTER",
                new ArrayList<>()
        );

        when(clothRepository.findById(testClothId)).thenReturn(Optional.of(testCloth));
        when(clothAttributeValueRepository.findAllByCloth_Id(testClothId)).thenReturn(new ArrayList<>());
        when(clothMapper.toDTO(any(Cloth.class))).thenReturn(
                new ClothDTO(testClothId, testUserId, "다크 블루 데님 자켓", null, "OUTER", new ArrayList<>())
        );
        when(clothMapper.toAttributeDTOs(any())).thenReturn(new ArrayList<>());

        // when
        ClothDTO result = clothService.updateCloth(testClothId, request, testUser.getId(), false, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("다크 블루 데님 자켓");
    }

    @Test
    @DisplayName("의상 수정 실패 - 권한 없음")
    void updateCloth_Unauthorized() {
        // given
        ClothUpdateRequest request = new ClothUpdateRequest("새 이름", "OUTER", new ArrayList<>());
        UUID anotherUserId = UUID.randomUUID();

        when(clothRepository.findById(testClothId)).thenReturn(Optional.of(testCloth));

        // when & then
        assertThatThrownBy(() -> clothService.updateCloth(testClothId, request, anotherUserId, false, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("권한이 없습니다");
    }

    @Test
    @DisplayName("의상 수정 성공 - 관리자")
    void updateCloth_SuccessAsAdmin() {
        // given
        ClothUpdateRequest request = new ClothUpdateRequest("새 이름", "OUTER", new ArrayList<>());
        UUID adminUserId = UUID.randomUUID();

        when(clothRepository.findById(testClothId)).thenReturn(Optional.of(testCloth));
        when(clothAttributeValueRepository.findAllByCloth_Id(testClothId)).thenReturn(new ArrayList<>());
        when(clothMapper.toDTO(any(Cloth.class))).thenReturn(
                new ClothDTO(testClothId, testUserId, "새 이름", null, "OUTER", new ArrayList<>())
        );
        when(clothMapper.toAttributeDTOs(any())).thenReturn(new ArrayList<>());

        // when
        ClothDTO result = clothService.updateCloth(testClothId, request, adminUserId, true, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("새 이름");
        verify(testCloth).updateName("새 이름");
    }

    @Test
    @DisplayName("의상 삭제 성공 - 소유자")
    void deleteCloth_SuccessAsOwner() {
        // given
        when(clothRepository.findById(testClothId)).thenReturn(Optional.of(testCloth));

        // when
        clothService.deleteCloth(testClothId, testUser.getId(), false);

        // then
        verify(clothAttributeValueRepository, times(1)).deleteAllByCloth_Id(testClothId);
        verify(clothRepository, times(1)).delete(testCloth);
    }

    @Test
    @DisplayName("의상 삭제 실패 - 권한 없음")
    void deleteCloth_Unauthorized() {
        // given
        UUID anotherUserId = UUID.randomUUID();
        when(clothRepository.findById(testClothId)).thenReturn(Optional.of(testCloth));

        // when & then
        assertThatThrownBy(() -> clothService.deleteCloth(testClothId, anotherUserId, false))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("권한이 없습니다");

        verify(clothRepository, never()).delete(any());
    }

    @Test
    @DisplayName("의상 삭제 성공 - 관리자")
    void deleteCloth_SuccessAsAdmin() {
        // given
        UUID adminUserId = UUID.randomUUID();
        when(clothRepository.findById(testClothId)).thenReturn(Optional.of(testCloth));

        // when
        clothService.deleteCloth(testClothId, adminUserId, true);

        // then
        verify(clothRepository, times(1)).delete(testCloth);
    }

    @Test
    @DisplayName("의상 삭제 실패 - 존재하지 않는 의상")
    void deleteCloth_NotFound() {
        // given
        when(clothRepository.findById(testClothId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> clothService.deleteCloth(testClothId, testUserId, false))
                .isInstanceOf(ClothNotFoundException.class);

        verify(clothRepository, never()).delete(any());
    }

    @Test
    @DisplayName("이미지와 함께 의상 생성 성공")
    void createCloth_WithImage() {
        // given
        ClothCreateRequest request = new ClothCreateRequest(
                testUserId,
                "블루 데님 자켓",
                "OUTER",
                new ArrayList<>()
        );

        BinaryContent binaryContent = BinaryContent.builder()
                .fileUrl("https://example.com/image.jpg")
                .build();

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(clothRepository.existsByOwner_IdAndName(testUserId, request.name())).thenReturn(false);
        when(binaryContentService.createBinaryContent(any())).thenReturn(binaryContent);
        when(clothRepository.save(any(Cloth.class))).thenReturn(testCloth);
        when(clothAttributeValueRepository.findAllByCloth_Id(any())).thenReturn(new ArrayList<>());
        when(clothMapper.toDTO(any(Cloth.class))).thenReturn(
                new ClothDTO(testClothId, testUserId, "블루 데님 자켓", "https://example.com/image.jpg", "OUTER", new ArrayList<>())
        );
        when(clothMapper.toAttributeDTOs(any())).thenReturn(new ArrayList<>());

        // when
        ClothDTO result = clothService.createCloth(request, mock(org.springframework.web.multipart.MultipartFile.class));

        // then
        assertThat(result).isNotNull();
        verify(binaryContentService, times(1)).createBinaryContent(any());
    }

    @Test
    @DisplayName("속성과 함께 의상 생성 성공")
    void createCloth_WithAttributes() {
        // given
        List<ClothAttributeValueDTO> attributes = List.of(
                new ClothAttributeValueDTO(UUID.randomUUID(), "블루")
        );

        ClothCreateRequest request = new ClothCreateRequest(
                testUserId,
                "블루 데님 자켓",
                "OUTER",
                attributes
        );

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(clothRepository.existsByOwner_IdAndName(testUserId, request.name())).thenReturn(false);
        when(clothRepository.save(any(Cloth.class))).thenReturn(testCloth);
        when(clothAttributeValueRepository.save(any())).thenReturn(mock(ClothAttributeValue.class));
        when(clothAttributeValueRepository.findAllByCloth_Id(any())).thenReturn(new ArrayList<>());
        when(clothMapper.toDTO(any(Cloth.class))).thenReturn(
                new ClothDTO(testClothId, testUserId, "블루 데님 자켓", null, "OUTER", attributes)
        );
        when(clothMapper.toAttributeDTOs(any())).thenReturn(
                List.of(new ClothAttributeValueDTO(UUID.randomUUID(), "블루"))
        );

        // when
        ClothDTO result = clothService.createCloth(request, null);

        // then
        assertThat(result).isNotNull();
        verify(clothAttributeValueRepository, times(1)).save(any());
    }
}
