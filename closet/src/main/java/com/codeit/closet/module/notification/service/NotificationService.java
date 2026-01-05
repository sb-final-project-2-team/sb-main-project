package com.codeit.closet.module.notification.service;

import java.util.List;
import java.util.UUID;


import com.codeit.closet.module.notification.dto.NotificationDTO;


public interface NotificationService {

	List<NotificationDTO> findAllByReceiverId(UUID receiverId);

	void delete(UUID id, UUID receiverId);

}
