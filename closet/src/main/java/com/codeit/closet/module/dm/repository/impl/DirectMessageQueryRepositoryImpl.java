package com.codeit.closet.module.dm.repository.impl;

import com.codeit.closet.module.dm.entity.QDirectMessage;
import com.codeit.closet.module.dm.dto.DirectMessageDTO;
import com.codeit.closet.module.dm.dto.DirectMessageDTOCursorResponse;
import com.codeit.closet.module.dm.entity.DirectMessage;
import com.codeit.closet.module.dm.mapper.DirectMessageMapper;
import com.codeit.closet.module.dm.repository.DirectMessageQueryRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class DirectMessageQueryRepositoryImpl implements DirectMessageQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;
    private final DirectMessageMapper directMessageMapper;

    private static final QDirectMessage directMessage = QDirectMessage.directMessage;

    @Override
    public DirectMessageDTOCursorResponse findDirectMessagesByDmKey(
            String dmKey,
            String cursor,
            Integer limit
    ) {
        int pageSize = (limit != null) ? limit : 20;

        CursorInfo cursorInfo = parseCursor(cursor);

        BooleanBuilder where = new BooleanBuilder();
        where.and(directMessage.dmKey.eq(dmKey));

        if (cursorInfo != null) {
            where.and(cursorConditionForOlder(cursorInfo));
        }

        List<DirectMessage> messages = jpaQueryFactory
                .selectFrom(directMessage)
                .leftJoin(directMessage.sender).fetchJoin()
                .leftJoin(directMessage.receiver).fetchJoin()
                .where(where)
                .orderBy(orderSpecifiersDesc())
                .limit(pageSize + 1)
                .fetch();

        Long totalCount = jpaQueryFactory
                .select(directMessage.count())
                .from(directMessage)
                .where(directMessage.dmKey.eq(dmKey))
                .fetchOne();

        boolean hasNext = messages.size() > pageSize;
        if (hasNext) {
            messages.remove(pageSize);
        }

        // 화면용 오름차순 정렬
        Collections.reverse(messages);

        String nextCursor = null;
        UUID nextIdAfter = null;

        if (!messages.isEmpty()) {
            DirectMessage oldest = messages.get(0);
            nextCursor = encodeCursor(oldest);
            nextIdAfter = oldest.getId();
        }

        List<DirectMessageDTO> data = directMessageMapper.toDTOs(messages);

        return new DirectMessageDTOCursorResponse(
                data,
                nextCursor,
                nextIdAfter,
                hasNext,
                totalCount != null ? totalCount : 0L,
                "createdAt",
                "ASCENDING"
        );
    }

    private record CursorInfo(Instant createdAt, UUID id) {}

    private CursorInfo parseCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }

        try {
            String decoded = new String(
                    Base64.getDecoder().decode(cursor),
                    StandardCharsets.UTF_8
            );

            String[] parts = decoded.split("\\|");
            if (parts.length < 2) {
                return null;
            }

            return new CursorInfo(
                    Instant.parse(parts[0]),
                    UUID.fromString(parts[1])
            );
        } catch (IllegalArgumentException | DateTimeParseException e) {
            return null;
        }
    }

    private String encodeCursor(DirectMessage message) {
        String raw = message.getCreatedAt() + "|" + message.getId();
        return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    private OrderSpecifier<?>[] orderSpecifiersDesc() {
        return new OrderSpecifier[]{
                directMessage.createdAt.desc(),
                directMessage.id.desc()
        };
    }

    private BooleanBuilder cursorConditionForOlder(CursorInfo cursor) {
        BooleanBuilder b = new BooleanBuilder();

        b.and(
                directMessage.createdAt.lt(cursor.createdAt)
                        .or(
                                directMessage.createdAt.eq(cursor.createdAt)
                                        .and(directMessage.id.lt(cursor.id))
                        )
        );

        return b;
    }
}
