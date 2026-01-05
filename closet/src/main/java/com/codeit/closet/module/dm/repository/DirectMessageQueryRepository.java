package com.codeit.closet.module.dm.repository;

import com.codeit.closet.module.dm.dto.DirectMessageDTOCursorResponse;

public interface DirectMessageQueryRepository {
    DirectMessageDTOCursorResponse findDirectMessagesByDmKey(
            String dmKey,
            String cursor,
            Integer limit
    );
}