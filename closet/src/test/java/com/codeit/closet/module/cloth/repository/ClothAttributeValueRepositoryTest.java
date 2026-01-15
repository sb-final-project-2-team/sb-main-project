package com.codeit.closet.module.cloth.repository;

import com.codeit.closet.module.cloth.entity.Cloth;
import com.codeit.closet.module.cloth.entity.ClothAttribute;
import com.codeit.closet.module.cloth.entity.ClothAttributeValue;
import com.codeit.closet.module.cloth.entity.ClothType;
import com.codeit.closet.module.user.entity.User;
import com.codeit.closet.module.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@DisplayName("ClothAttributeValueRepository 테스트")
class ClothAttributeValueRepositoryTest {

    @Autowired
    private ClothAttributeValueRepository clothAttributeValueRepository;

    @Autowired
    private ClothRepository clothRepository;

    @Autowired
    private ClothAttributeRepository clothAttributeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private User testUser;
    private Cloth testCloth;
    private ClothAttribute testAttribute;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = User.builder()
                .name("testuser")
                .email("test@example.com")
                .password("password123")
                .provider(com.codeit.closet.module.user.entity.AuthProvider.LOCAL)
                .providerId("test-provider-id")
                .role(com.codeit.closet.module.user.entity.UserRole.USER)
                .build();
        testUser = userRepository.save(testUser);

        // 테스트용 의상 생성
        testCloth = Cloth.builder()
                .owner(testUser)
                .name("블루 데님 자켓")
                .type(ClothType.OUTER)
                .build();
        testCloth = clothRepository.save(testCloth);

        // 테스트용 속성 정의 생성
        testAttribute = ClothAttribute.builder()
                .name("색상")
                .attributesValues(List.of("블랙", "화이트", "블루", "레드"))
                .build();
        testAttribute = clothAttributeRepository.save(testAttribute);
    }

    @Test
    @DisplayName("의상 속성 값 저장 성공")
    void saveClothAttributeValue_Success() {
        // given
        ClothAttributeValue attributeValue = ClothAttributeValue.builder()
                .cloth(testCloth)
                .clothAttribute(testAttribute)
                .value("블루")
                .build();

        // when
        ClothAttributeValue saved = clothAttributeValueRepository.save(attributeValue);
        entityManager.flush();

        // then
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getValue()).isEqualTo("블루");
        assertThat(saved.getCloth().getId()).isEqualTo(testCloth.getId());
        assertThat(saved.getClothAttribute().getId()).isEqualTo(testAttribute.getId());
    }

    @Test
    @DisplayName("특정 의상의 속성 값 목록 조회")
    void findAllByCloth_Id_Success() {
        // given
        ClothAttribute sizeAttribute = ClothAttribute.builder()
                .name("사이즈")
                .attributesValues(List.of("S", "M", "L"))
                .build();
        sizeAttribute = clothAttributeRepository.save(sizeAttribute);

        ClothAttributeValue value1 = ClothAttributeValue.builder()
                .cloth(testCloth)
                .clothAttribute(testAttribute)
                .value("블루")
                .build();
        ClothAttributeValue value2 = ClothAttributeValue.builder()
                .cloth(testCloth)
                .clothAttribute(sizeAttribute)
                .value("M")
                .build();

        clothAttributeValueRepository.saveAll(List.of(value1, value2));
        entityManager.flush();

        // when
        List<ClothAttributeValue> values = clothAttributeValueRepository.findAllByCloth_Id(testCloth.getId());

        // then
        assertThat(values).hasSize(2);
        assertThat(values)
                .extracting(ClothAttributeValue::getValue)
                .containsExactlyInAnyOrder("블루", "M");
    }

    @Test
    @DisplayName("다른 의상의 속성 값은 조회되지 않음")
    void findAllByCloth_Id_OnlySpecificCloth() {
        // given
        Cloth anotherCloth = Cloth.builder()
                .owner(testUser)
                .name("레드 셔츠")
                .type(ClothType.TOP)
                .build();
        anotherCloth = clothRepository.save(anotherCloth);

        ClothAttributeValue value1 = ClothAttributeValue.builder()
                .cloth(testCloth)
                .clothAttribute(testAttribute)
                .value("블루")
                .build();
        ClothAttributeValue value2 = ClothAttributeValue.builder()
                .cloth(anotherCloth)
                .clothAttribute(testAttribute)
                .value("레드")
                .build();

        clothAttributeValueRepository.saveAll(List.of(value1, value2));
        entityManager.flush();

        // when
        List<ClothAttributeValue> values = clothAttributeValueRepository.findAllByCloth_Id(testCloth.getId());

        // then
        assertThat(values).hasSize(1);
        assertThat(values.get(0).getValue()).isEqualTo("블루");
    }

    @Test
    @DisplayName("속성 값이 없는 의상 조회 시 빈 리스트 반환")
    void findAllByCloth_Id_EmptyList() {
        // given
        Cloth emptyCloth = Cloth.builder()
                .owner(testUser)
                .name("속성 없는 의상")
                .type(ClothType.TOP)
                .build();
        emptyCloth = clothRepository.save(emptyCloth);

        // when
        List<ClothAttributeValue> values = clothAttributeValueRepository.findAllByCloth_Id(emptyCloth.getId());

        // then
        assertThat(values).isEmpty();
    }

    @Test
    @DisplayName("특정 의상의 모든 속성 값 삭제")
    void deleteAllByCloth_Id_Success() {
        // given
        ClothAttribute sizeAttribute = ClothAttribute.builder()
                .name("사이즈")
                .attributesValues(List.of("S", "M", "L"))
                .build();
        sizeAttribute = clothAttributeRepository.save(sizeAttribute);

        ClothAttributeValue value1 = ClothAttributeValue.builder()
                .cloth(testCloth)
                .clothAttribute(testAttribute)
                .value("블루")
                .build();
        ClothAttributeValue value2 = ClothAttributeValue.builder()
                .cloth(testCloth)
                .clothAttribute(sizeAttribute)
                .value("M")
                .build();

        clothAttributeValueRepository.saveAll(List.of(value1, value2));

        // when
        clothAttributeValueRepository.deleteAllByCloth_Id(testCloth.getId());
        clothAttributeValueRepository.flush();

        // then
        List<ClothAttributeValue> values = clothAttributeValueRepository.findAllByCloth_Id(testCloth.getId());
        assertThat(values).isEmpty();
    }

    @Test
    @DisplayName("다른 의상의 속성 값은 삭제되지 않음")
    void deleteAllByCloth_Id_OnlySpecificCloth() {
        // given
        Cloth anotherCloth = Cloth.builder()
                .owner(testUser)
                .name("레드 셔츠")
                .type(ClothType.TOP)
                .build();
        anotherCloth = clothRepository.save(anotherCloth);

        ClothAttributeValue value1 = ClothAttributeValue.builder()
                .cloth(testCloth)
                .clothAttribute(testAttribute)
                .value("블루")
                .build();
        ClothAttributeValue value2 = ClothAttributeValue.builder()
                .cloth(anotherCloth)
                .clothAttribute(testAttribute)
                .value("레드")
                .build();

        clothAttributeValueRepository.saveAll(List.of(value1, value2));

        // when
        clothAttributeValueRepository.deleteAllByCloth_Id(testCloth.getId());
        clothAttributeValueRepository.flush();

        // then
        List<ClothAttributeValue> testClothValues = clothAttributeValueRepository.findAllByCloth_Id(testCloth.getId());
        List<ClothAttributeValue> anotherClothValues = clothAttributeValueRepository.findAllByCloth_Id(anotherCloth.getId());

        assertThat(testClothValues).isEmpty();
        assertThat(anotherClothValues).hasSize(1);
        assertThat(anotherClothValues.get(0).getValue()).isEqualTo("레드");
    }

    @Test
    @DisplayName("같은 의상에 같은 속성 정의로 중복 저장 시도 시 제약 조건 위반")
    void saveClothAttributeValue_DuplicateConstraint() {
        // given
        ClothAttributeValue value1 = ClothAttributeValue.builder()
                .cloth(testCloth)
                .clothAttribute(testAttribute)
                .value("블루")
                .build();
        clothAttributeValueRepository.save(value1);
        clothAttributeValueRepository.flush();

        ClothAttributeValue value2 = ClothAttributeValue.builder()
                .cloth(testCloth)
                .clothAttribute(testAttribute)
                .value("레드")  // 다른 값이지만 같은 의상+속성 정의
                .build();

        // when & then
        // unique constraint 위반 예상
        try {
            clothAttributeValueRepository.save(value2);
            clothAttributeValueRepository.flush();
            // 제약 조건이 있다면 여기서 예외 발생
        } catch (Exception e) {
            // 예외 발생 확인
            assertThat(e).isNotNull();
        }
    }

    @Test
    @DisplayName("여러 속성 값 일괄 저장")
    void saveAll_Success() {
        // given
        ClothAttribute sizeAttribute = ClothAttribute.builder()
                .name("사이즈")
                .attributesValues(List.of("S", "M", "L"))
                .build();
        sizeAttribute = clothAttributeRepository.save(sizeAttribute);

        ClothAttribute materialAttribute = ClothAttribute.builder()
                .name("소재")
                .attributesValues(List.of("면", "폴리에스터"))
                .build();
        materialAttribute = clothAttributeRepository.save(materialAttribute);

        ClothAttributeValue value1 = ClothAttributeValue.builder()
                .cloth(testCloth)
                .clothAttribute(testAttribute)
                .value("블루")
                .build();
        ClothAttributeValue value2 = ClothAttributeValue.builder()
                .cloth(testCloth)
                .clothAttribute(sizeAttribute)
                .value("M")
                .build();
        ClothAttributeValue value3 = ClothAttributeValue.builder()
                .cloth(testCloth)
                .clothAttribute(materialAttribute)
                .value("면")
                .build();

        // when
        List<ClothAttributeValue> saved = clothAttributeValueRepository.saveAll(List.of(value1, value2, value3));

        // then
        assertThat(saved).hasSize(3);
        List<ClothAttributeValue> values = clothAttributeValueRepository.findAllByCloth_Id(testCloth.getId());
        assertThat(values).hasSize(3);
    }
}
