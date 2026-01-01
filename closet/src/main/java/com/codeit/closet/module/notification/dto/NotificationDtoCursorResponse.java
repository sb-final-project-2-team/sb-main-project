package com.codeit.closet.module.notification.dto;

import java.util.List;
import java.util.UUID;

public record NotificationDtoCursorResponse(
	List<NotificationDto> data,
	String nextCursor,
	UUID nextIdAfter,
	boolean hasNext,
	long totalCount,
	String sortBy,
	String sortDirection
) {}
