package com.taskflow.taskflow_be.module.notification.mapper;

import com.taskflow.taskflow_be.module.notification.dto.NotificationDtos;
import com.taskflow.taskflow_be.module.notification.entity.NotificationEntity;

public class NotificationMapper {

    public static NotificationDtos.NotificationResponse toResponse(NotificationEntity entity) {
        var dto = new NotificationDtos.NotificationResponse();
        dto.setId(entity.getId());
        dto.setType(entity.getType());
        dto.setTitle(entity.getTitle());
        dto.setBody(entity.getBody());
        dto.setRead(entity.isRead());
        dto.setCreatedAt(entity.getCreatedAt());

        if (entity.getActor() != null) {
            var actor = new NotificationDtos.ActorResponse();
            actor.setId(entity.getActor().getId());
            actor.setUsername(entity.getActor().getUsername());
            actor.setFullName(entity.getActor().getFullName());
            dto.setActor(actor);
        }

        if (entity.getEntityType() != null || entity.getEntityId() != null || entity.getRoute() != null) {
            var target = new NotificationDtos.TargetResponse();
            target.setEntityType(entity.getEntityType());
            target.setEntityId(entity.getEntityId());
            target.setRoute(entity.getRoute());
            dto.setTarget(target);
        }

        return dto;
    }
}
