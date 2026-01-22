package com.codeit.closet.module.cloth.exception;

import com.codeit.closet.common.exception.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ClothExceptionHandler 테스트")
class ClothExceptionHandlerTest {

    private ClothExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new ClothExceptionHandler();
    }

    @Test
    @DisplayName("ClothNotFoundException 처리")
    void handleClothNotFound() {
        // given
        UUID clothId = UUID.randomUUID();
        ClothNotFoundException exception = new ClothNotFoundException(clothId);

        // when
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleClothNotFound(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("DuplicateClothNameException 처리")
    void handleDuplicateClothName() {
        // given
        DuplicateClothNameException exception = new DuplicateClothNameException("테스트 의상");

        // when
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDuplicateClothName(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
    }

    @Test
    @DisplayName("InvalidClothAttributeException 처리")
    void handleInvalidClothAttribute() {
        // given
        InvalidClothAttributeException exception = new InvalidClothAttributeException("잘못된 속성");

        // when
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidClothAttribute(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("ClothAttributeNotFoundException 처리")
    void handleClothAttributeNotFound() {
        // given
        UUID attributeId = UUID.randomUUID();
        ClothAttributeNotFoundException exception = new ClothAttributeNotFoundException(attributeId);

        // when
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleClothAttributeNotFound(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("DuplicateClothAttributeNameException 처리")
    void handleDuplicateClothAttributeName() {
        // given
        DuplicateClothAttributeNameException exception = new DuplicateClothAttributeNameException("색상");

        // when
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDuplicateClothAttributeName(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
    }
}
