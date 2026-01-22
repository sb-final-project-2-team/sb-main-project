package com.codeit.closet.module.cloth.mapper;

import com.codeit.closet.module.binarycontent.entity.BinaryContent;
import com.codeit.closet.module.cloth.dto.ClothAttributeValueDTO;
import com.codeit.closet.module.cloth.dto.ClothDTO;
import com.codeit.closet.module.cloth.entity.Cloth;
import com.codeit.closet.module.cloth.entity.ClothAttribute;
import com.codeit.closet.module.cloth.entity.ClothAttributeValue;
import com.codeit.closet.module.cloth.entity.ClothType;
import com.codeit.closet.module.user.entity.AuthProvider;
import com.codeit.closet.module.user.entity.User;
import com.codeit.closet.module.user.entity.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("ClothMapper 통합 테스트")
class ClothMapperTest {

    @Autowired
    private ClothMapper clothMapper;

    @Test
    @DisplayName("toDTO - BinaryContent가 있는 경우")
    void toDTO_WithBinaryContent() {
        // given
        User owner = User.builder()
                .name("testuser")
                .email("test@example.com")
                .password("password")
                .role(UserRole.USER)
                .provider(AuthProvider.LOCAL)
                .providerId("local-123")
                .build();

        BinaryContent binaryContent = BinaryContent.builder()
                .fileUrl("https://example.com/cloth-image.jpg")
                .fileName("cloth-image.jpg")
                .size(2048L)
                .contentType("image/jpeg")
                .build();

        Cloth cloth = Cloth.builder()
                .name("Blue Jeans")
                .owner(owner)
                .binaryContent(binaryContent)
                .type(ClothType.BOTTOM)
                .build();

        // when
        ClothDTO result = clothMapper.toDTO(cloth);

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Blue Jeans");
        assertThat(result.imageUrl()).isEqualTo("https://example.com/cloth-image.jpg");
        assertThat(result.type()).isEqualTo("BOTTOM");
        assertThat(result.ownerId()).isNull(); // owner.id는 builder에서 설정 안함
    }

    @Test
    @DisplayName("toDTO - BinaryContent가 null인 경우")
    void toDTO_WithoutBinaryContent() {
        // given
        User owner = User.builder()
                .name("testuser2")
                .email("test2@example.com")
                .password("password")
                .role(UserRole.USER)
                .provider(AuthProvider.LOCAL)
                .providerId("local-456")
                .build();

        Cloth cloth = Cloth.builder()
                .name("White T-Shirt")
                .owner(owner)
                .binaryContent(null)
                .type(ClothType.TOP)
                .build();

        // when
        ClothDTO result = clothMapper.toDTO(cloth);

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("White T-Shirt");
        assertThat(result.imageUrl()).isNull();
        assertThat(result.type()).isEqualTo("TOP");
    }

    @Test
    @DisplayName("toDTO - 다양한 ClothType 테스트")
    void toDTO_VariousClothTypes() {
        // given
        User owner = User.builder()
                .name("testuser")
                .email("test@example.com")
                .password("password")
                .role(UserRole.USER)
                .provider(AuthProvider.LOCAL)
                .providerId("local-789")
                .build();

        Cloth dressCloth = Cloth.builder()
                .name("Summer Dress")
                .owner(owner)
                .type(ClothType.DRESS)
                .build();

        Cloth outerCloth = Cloth.builder()
                .name("Winter Jacket")
                .owner(owner)
                .type(ClothType.OUTER)
                .build();

        // when
        ClothDTO dressResult = clothMapper.toDTO(dressCloth);
        ClothDTO outerResult = clothMapper.toDTO(outerCloth);

        // then
        assertThat(dressResult.type()).isEqualTo("DRESS");
        assertThat(outerResult.type()).isEqualTo("OUTER");
    }

    @Test
    @DisplayName("toDTOs - 리스트 변환 테스트")
    void toDTOs_Success() {
        // given
        User owner = User.builder()
                .name("testuser")
                .email("test@example.com")
                .password("password")
                .role(UserRole.USER)
                .provider(AuthProvider.LOCAL)
                .providerId("local-000")
                .build();

        Cloth cloth1 = Cloth.builder()
                .name("Cloth 1")
                .owner(owner)
                .type(ClothType.TOP)
                .build();

        Cloth cloth2 = Cloth.builder()
                .name("Cloth 2")
                .owner(owner)
                .type(ClothType.BOTTOM)
                .build();

        List<Cloth> clothes = List.of(cloth1, cloth2);

        // when
        List<ClothDTO> results = clothMapper.toDTOs(clothes);

        // then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).name()).isEqualTo("Cloth 1");
        assertThat(results.get(0).type()).isEqualTo("TOP");
        assertThat(results.get(1).name()).isEqualTo("Cloth 2");
        assertThat(results.get(1).type()).isEqualTo("BOTTOM");
    }

    @Test
    @DisplayName("toDTOs - 빈 리스트 처리")
    void toDTOs_EmptyList() {
        // given
        List<Cloth> clothes = List.of();

        // when
        List<ClothDTO> results = clothMapper.toDTOs(clothes);

        // then
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("toDTO(ClothAttributeValue) - 속성 값 변환")
    void toDTO_ClothAttributeValue() {
        // given
        ClothAttribute clothAttribute = ClothAttribute.builder()
                .name("Color")
                .attributesValues(List.of("Red", "Blue", "Green"))
                .build();

        ClothAttributeValue attributeValue = ClothAttributeValue.builder()
                .clothAttribute(clothAttribute)
                .value("Red")
                .build();

        // when
        ClothAttributeValueDTO result = clothMapper.toDTO(attributeValue);

        // then
        assertThat(result).isNotNull();
        assertThat(result.value()).isEqualTo("Red");
        assertThat(result.definitionId()).isNull(); // clothAttribute.id는 builder에서 설정 안함
    }

    @Test
    @DisplayName("toAttributeDTOs - 속성 값 리스트 변환")
    void toAttributeDTOs_Success() {
        // given
        ClothAttribute colorAttribute = ClothAttribute.builder()
                .name("Color")
                .attributesValues(List.of("Black", "White"))
                .build();

        ClothAttribute sizeAttribute = ClothAttribute.builder()
                .name("Size")
                .attributesValues(List.of("S", "M", "L"))
                .build();

        ClothAttributeValue colorValue = ClothAttributeValue.builder()
                .clothAttribute(colorAttribute)
                .value("Black")
                .build();

        ClothAttributeValue sizeValue = ClothAttributeValue.builder()
                .clothAttribute(sizeAttribute)
                .value("M")
                .build();

        List<ClothAttributeValue> attributeValues = List.of(colorValue, sizeValue);

        // when
        List<ClothAttributeValueDTO> results = clothMapper.toAttributeDTOs(attributeValues);

        // then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).value()).isEqualTo("Black");
        assertThat(results.get(1).value()).isEqualTo("M");
    }

    @Test
    @DisplayName("toAttributeDTOs - 빈 리스트 처리")
    void toAttributeDTOs_EmptyList() {
        // given
        List<ClothAttributeValue> attributeValues = List.of();

        // when
        List<ClothAttributeValueDTO> results = clothMapper.toAttributeDTOs(attributeValues);

        // then
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("toDTO - 모든 ClothType enum 값 테스트")
    void toDTO_AllClothTypes() {
        // given
        User owner = User.builder()
                .name("testuser")
                .email("test@example.com")
                .password("password")
                .role(UserRole.USER)
                .provider(AuthProvider.LOCAL)
                .providerId("local-all")
                .build();

        // when & then
        for (ClothType type : ClothType.values()) {
            Cloth cloth = Cloth.builder()
                    .name("Test " + type.name())
                    .owner(owner)
                    .type(type)
                    .build();

            ClothDTO result = clothMapper.toDTO(cloth);

            assertThat(result.type()).isEqualTo(type.name());
        }
    }

    @Test
    @DisplayName("toDTO - 특수 문자가 포함된 이름 처리")
    void toDTO_SpecialCharactersInName() {
        // given
        User owner = User.builder()
                .name("testuser")
                .email("test@example.com")
                .password("password")
                .role(UserRole.USER)
                .provider(AuthProvider.LOCAL)
                .providerId("local-special")
                .build();

        Cloth cloth = Cloth.builder()
                .name("Name with 한글 & Special!@# Characters")
                .owner(owner)
                .type(ClothType.ETC)
                .build();

        // when
        ClothDTO result = clothMapper.toDTO(cloth);

        // then
        assertThat(result.name()).isEqualTo("Name with 한글 & Special!@# Characters");
    }
}
