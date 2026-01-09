package com.codeit.closet.module.cloth.repository.impl;

import com.codeit.closet.module.cloth.dto.ClothDTO;
import com.codeit.closet.module.cloth.dto.ClothDTOCursorResponse;
import com.codeit.closet.module.cloth.entity.Cloth;
import com.codeit.closet.module.cloth.entity.QCloth;
import com.codeit.closet.module.cloth.mapper.ClothMapper;
import com.codeit.closet.module.cloth.repository.ClothAttributeValueRepository;
import com.codeit.closet.module.cloth.repository.ClothQueryRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ClothQueryRepositoryImpl implements ClothQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;
    private final ClothMapper clothMapper;
    private final ClothAttributeValueRepository clothAttributeValueRepository;

    private static final QCloth cloth = QCloth.cloth;

    @Override
    public ClothDTOCursorResponse findClothsByCursor(
            UUID ownerId,
            String cursor,
            UUID idAfter,
            Integer limit,
            String sortBy,
            String sortDirection,
            String typeEqual) {

        int pageSize = limit != null ? limit : 20;

        CursorInfo cursorInfo = parseCursor(cursor);

        BooleanBuilder builder = new BooleanBuilder();

        // ownerId 필터 (필수)
        builder.and(cloth.ownerId.eq(ownerId));

        // typeEqual 필터 (선택)
        if (typeEqual != null && !typeEqual.isEmpty()) {
            builder.and(cloth.type.eq(com.codeit.closet.module.cloth.entity.ClothType.valueOf(typeEqual)));
        }

        // 커서 조건
        if (cursorInfo != null) {
            builder.and(cursorCondition(cursorInfo, sortBy, sortDirection));
        }

        // 조회
        List<Cloth> clothes = jpaQueryFactory
                .selectFrom(cloth)
                .where(builder)
                .orderBy(orderSpecifiers(sortBy, sortDirection))
                .limit(pageSize + 1)
                .fetch();

        // 전체 개수
        Long totalCount = jpaQueryFactory
                .select(cloth.count())
                .from(cloth)
                .where(cloth.ownerId.eq(ownerId))
                .fetchOne();

        // hasNext 판단
        boolean hasNext = clothes.size() > pageSize;
        if (hasNext) {
            clothes.remove(pageSize);
        }

        // nextCursor 생성
        String nextCursor = null;
        UUID nextAfter = null;

        if (!clothes.isEmpty()) {
            Cloth last = clothes.get(clothes.size() - 1);
            nextCursor = encodeCursor(last);
            nextAfter = last.getId();
        }

        // DTO 변환 (attributes 포함)
        List<ClothDTO> clothDTOs = clothes.stream()
                .map(this::toDto)
                .toList();

        return new ClothDTOCursorResponse(
                clothDTOs,
                nextCursor,
                nextAfter,
                hasNext,
                totalCount.intValue(),
                sortBy != null ? sortBy : "createdAt",
                sortDirection != null ? sortDirection : "desc"
        );
    }

    // ==================== 커서 관련 ====================
    private record CursorInfo(Instant createdAt, UUID id) {
    }

    private CursorInfo parseCursor(String cursor) {
        if (cursor == null) {
            return null;
        }
        try {
            String decoded = new String(Base64.getDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = decoded.split("\\|");
            if (parts.length < 2) {
                return null;
            }
            return new CursorInfo(
                    Instant.parse(parts[0]),
                    UUID.fromString(parts[1])
            );
        } catch (IllegalArgumentException | DateTimeException e) {
            return null;
        }
    }

    private String encodeCursor(Cloth cloth) {
        String raw = cloth.getCreatedAt() + "|" + cloth.getId();
        return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    // ==================== 정렬 & 커서 조건 ====================
    private OrderSpecifier<?>[] orderSpecifiers(String sortBy, String sortDirection) {
        boolean desc = "desc".equalsIgnoreCase(sortDirection) || "DESCENDING".equalsIgnoreCase(sortDirection);

        if ("name".equalsIgnoreCase(sortBy)) {
            return new OrderSpecifier[]{
                    desc ? cloth.name.desc() : cloth.name.asc(),
                    desc ? cloth.id.desc() : cloth.id.asc()
            };
        }

        // 기본: createdAt 정렬
        return new OrderSpecifier[]{
                desc ? cloth.createdAt.desc() : cloth.createdAt.asc(),
                desc ? cloth.id.desc() : cloth.id.asc()
        };
    }

    private BooleanBuilder cursorCondition(CursorInfo cursor, String sortBy, String sortDirection) {
        BooleanBuilder builder = new BooleanBuilder();
        boolean desc = "desc".equalsIgnoreCase(sortDirection) || "DESCENDING".equalsIgnoreCase(sortDirection);

        if (desc) {
            builder.or(cloth.createdAt.lt(cursor.createdAt())
                    .or(cloth.createdAt.eq(cursor.createdAt())
                            .and(cloth.id.lt(cursor.id()))));
        } else {
            builder.or(cloth.createdAt.gt(cursor.createdAt())
                    .or(cloth.createdAt.eq(cursor.createdAt())
                            .and(cloth.id.gt(cursor.id()))));
        }

        return builder;
    }

    // Entity -> DTO 변환 (attributes 포함)
    private ClothDTO toDto(Cloth cloth) {
        var attributeValues = clothAttributeValueRepository.findAllByClothId(cloth.getId());
        var clothDTO = clothMapper.toDTO(cloth);
        var attributeDtos = clothMapper.toAttributeDTOs(attributeValues);

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
