package com.codeit.closet.module.cloth.repository;

import com.codeit.closet.module.cloth.entity.ClothAttribute;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@DisplayName("ClothAttributeRepository 테스트")
class ClothAttributeRepositoryTest {

    @Autowired
    private ClothAttributeRepository clothAttributeRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @Test
    @DisplayName("의상 속성 정의 저장 성공")
    void saveClothAttribute_Success() {
        // given
        ClothAttribute attribute = ClothAttribute.builder()
                .name("색상")
                .attributesValues(List.of("블랙", "화이트", "블루", "레드"))
                .build();

        // when
        ClothAttribute saved = clothAttributeRepository.save(attribute);
        entityManager.flush();

        // then
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("색상");
        assertThat(saved.getAttributesValues()).hasSize(4);
        assertThat(saved.getAttributesValues()).containsExactly("블랙", "화이트", "블루", "레드");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("ID로 속성 정의 조회 성공")
    void findById_Success() {
        // given
        ClothAttribute attribute = ClothAttribute.builder()
                .name("사이즈")
                .attributesValues(List.of("S", "M", "L", "XL"))
                .build();
        ClothAttribute saved = clothAttributeRepository.save(attribute);
        entityManager.flush();

        // when
        Optional<ClothAttribute> found = clothAttributeRepository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("사이즈");
        assertThat(found.get().getAttributesValues()).containsExactly("S", "M", "L", "XL");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회 시 빈 Optional 반환")
    void findById_NotFound() {
        // given
        UUID nonExistentId = UUID.randomUUID();

        // when
        Optional<ClothAttribute> found = clothAttributeRepository.findById(nonExistentId);

        // then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("이름으로 중복 검사 - 존재하는 경우")
    void existsByName_Exists() {
        // given
        ClothAttribute attribute = ClothAttribute.builder()
                .name("색상")
                .attributesValues(List.of("블랙", "화이트"))
                .build();
        clothAttributeRepository.save(attribute);
        entityManager.flush();

        // when
        boolean exists = clothAttributeRepository.existsByName("색상");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("이름으로 중복 검사 - 존재하지 않는 경우")
    void existsByName_NotExists() {
        // given
        ClothAttribute attribute = ClothAttribute.builder()
                .name("색상")
                .attributesValues(List.of("블랙", "화이트"))
                .build();
        clothAttributeRepository.save(attribute);

        // when
        boolean exists = clothAttributeRepository.existsByName("패턴");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("속성 정의 업데이트 성공")
    void updateClothAttribute_Success() {
        // given
        ClothAttribute attribute = ClothAttribute.builder()
                .name("색상")
                .attributesValues(List.of("블랙", "화이트"))
                .build();
        ClothAttribute saved = clothAttributeRepository.save(attribute);

        // when
        saved.updateName("컬러");
        saved.updateAttributesValues(List.of("블랙", "화이트", "블루", "레드", "그레이"));
        ClothAttribute updated = clothAttributeRepository.save(saved);

        // then
        assertThat(updated.getName()).isEqualTo("컬러");
        assertThat(updated.getAttributesValues()).hasSize(5);
        assertThat(updated.getAttributesValues()).containsExactly("블랙", "화이트", "블루", "레드", "그레이");
    }

    @Test
    @DisplayName("속성 정의 삭제 성공")
    void deleteClothAttribute_Success() {
        // given
        ClothAttribute attribute = ClothAttribute.builder()
                .name("색상")
                .attributesValues(List.of("블랙", "화이트"))
                .build();
        ClothAttribute saved = clothAttributeRepository.save(attribute);

        // when
        clothAttributeRepository.deleteById(saved.getId());

        // then
        Optional<ClothAttribute> found = clothAttributeRepository.findById(saved.getId());
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("모든 속성 정의 조회")
    void findAll_Success() {
        // given
        ClothAttribute attribute1 = ClothAttribute.builder()
                .name("색상")
                .attributesValues(List.of("블랙", "화이트"))
                .build();
        ClothAttribute attribute2 = ClothAttribute.builder()
                .name("사이즈")
                .attributesValues(List.of("S", "M", "L"))
                .build();
        ClothAttribute attribute3 = ClothAttribute.builder()
                .name("소재")
                .attributesValues(List.of("면", "폴리에스터", "나일론"))
                .build();

        clothAttributeRepository.saveAll(List.of(attribute1, attribute2, attribute3));
        entityManager.flush();

        // when
        List<ClothAttribute> allAttributes = clothAttributeRepository.findAll();

        // then
        assertThat(allAttributes).hasSizeGreaterThanOrEqualTo(3);
        assertThat(allAttributes)
                .extracting(ClothAttribute::getName)
                .contains("색상", "사이즈", "소재");
    }

    @Test
    @DisplayName("빈 값 리스트로 속성 정의 생성")
    void saveClothAttribute_WithEmptyValues() {
        // given
        ClothAttribute attribute = ClothAttribute.builder()
                .name("빈속성")
                .attributesValues(List.of())
                .build();

        // when
        ClothAttribute saved = clothAttributeRepository.save(attribute);

        // then
        assertThat(saved).isNotNull();
        assertThat(saved.getAttributesValues()).isEmpty();
    }

    @Test
    @DisplayName("단일 값으로 속성 정의 생성")
    void saveClothAttribute_WithSingleValue() {
        // given
        ClothAttribute attribute = ClothAttribute.builder()
                .name("특별속성")
                .attributesValues(List.of("유일한값"))
                .build();

        // when
        ClothAttribute saved = clothAttributeRepository.save(attribute);

        // then
        assertThat(saved).isNotNull();
        assertThat(saved.getAttributesValues()).hasSize(1);
        assertThat(saved.getAttributesValues()).containsExactly("유일한값");
    }

    @Test
    @DisplayName("속성 정의 부분 업데이트 - 이름만 변경")
    void updateClothAttribute_NameOnly() {
        // given
        ClothAttribute attribute = ClothAttribute.builder()
                .name("색상")
                .attributesValues(List.of("블랙", "화이트"))
                .build();
        ClothAttribute saved = clothAttributeRepository.save(attribute);

        // when
        saved.updateName("색깔");
        ClothAttribute updated = clothAttributeRepository.save(saved);

        // then
        assertThat(updated.getName()).isEqualTo("색깔");
        assertThat(updated.getAttributesValues()).hasSize(2);
    }

    @Test
    @DisplayName("속성 정의 부분 업데이트 - 값 목록만 변경")
    void updateClothAttribute_ValuesOnly() {
        // given
        ClothAttribute attribute = ClothAttribute.builder()
                .name("색상")
                .attributesValues(List.of("블랙", "화이트"))
                .build();
        ClothAttribute saved = clothAttributeRepository.save(attribute);

        // when
        saved.updateAttributesValues(List.of("블랙", "화이트", "블루"));
        ClothAttribute updated = clothAttributeRepository.save(saved);

        // then
        assertThat(updated.getName()).isEqualTo("색상");
        assertThat(updated.getAttributesValues()).hasSize(3);
    }
}
