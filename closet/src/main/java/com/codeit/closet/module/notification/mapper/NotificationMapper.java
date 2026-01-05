package com.codeit.closet.module.notification.mapper;

import org.mapstruct.Mapper;

import com.codeit.closet.module.notification.dto.NotificationDTO;
import com.codeit.closet.module.notification.entity.Notification;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
	NotificationDTO toDto(Notification entity);
}


