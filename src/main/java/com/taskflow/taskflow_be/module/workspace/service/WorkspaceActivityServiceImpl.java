package com.taskflow.taskflow_be.module.workspace.service;

import com.taskflow.taskflow_be.common.util.SecurityUtils;
import com.taskflow.taskflow_be.module.auth.entity.UserEntity;
import com.taskflow.taskflow_be.module.auth.repository.UserRepository;
import com.taskflow.taskflow_be.module.issue.entity.IssueActivityEntity;
import com.taskflow.taskflow_be.module.issue.entity.IssueActivityType;
import com.taskflow.taskflow_be.module.issue.repository.IssueActivityRepository;
import com.taskflow.taskflow_be.module.workspace.dto.WorkspaceActivityDtos;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class WorkspaceActivityServiceImpl implements WorkspaceActivityService {

    private final IssueActivityRepository issueActivityRepository;
    private final UserRepository userRepository;

    public WorkspaceActivityServiceImpl(
            IssueActivityRepository issueActivityRepository,
            UserRepository userRepository
    ) {
        this.issueActivityRepository = issueActivityRepository;
        this.userRepository = userRepository;
    }

    @Override
    public WorkspaceActivityDtos.ActivityPageResponse getActivities(
            String q,
            UUID projectId,
            String actor,
            IssueActivityType type,
            int page,
            int pageSize
    ) {
        UUID currentUserId = getCurrentUser().getId();

        int safePage = Math.max(page, 1);
        int safePageSize = Math.max(pageSize, 1);

        var pageable = PageRequest.of(
                safePage - 1,
                safePageSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        var actorFilter = actor != null && !actor.isBlank() ? actor.trim() : null;

        var result = actorFilter == null
                ? issueActivityRepository.findWorkspaceActivities(
                currentUserId,
                projectId,
                type,
                pageable
        )
                : issueActivityRepository.findWorkspaceActivitiesByActor(
                currentUserId,
                projectId,
                actorFilter,
                type,
                pageable
        );

        var response = new WorkspaceActivityDtos.ActivityPageResponse();
        response.setItems(result.getContent().stream().map(this::toResponse).toList());
        response.setPage(safePage);
        response.setPageSize(safePageSize);
        response.setTotal(result.getTotalElements());
        return response;
    }

    private UserEntity getCurrentUser() {
        var username = SecurityUtils.currentUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }

    private WorkspaceActivityDtos.ActivityResponse toResponse(IssueActivityEntity entity) {
        var dto = new WorkspaceActivityDtos.ActivityResponse();
        dto.setId(entity.getId());
        dto.setType(entity.getType().name());
        dto.setMessage(buildMessage(entity));
        dto.setCreatedAt(entity.getCreatedAt());

        var actor = new WorkspaceActivityDtos.ActorResponse();
        actor.setId(entity.getActor().getId());
        actor.setUsername(entity.getActor().getUsername());
        actor.setFullName(entity.getActor().getFullName());
        dto.setActor(actor);

        var project = new WorkspaceActivityDtos.ProjectResponse();
        project.setId(entity.getIssue().getProject().getId());
        project.setKey(entity.getIssue().getProject().getKey());
        project.setName(entity.getIssue().getProject().getName());
        dto.setProject(project);

        var target = new WorkspaceActivityDtos.TargetResponse();
        target.setEntityType("ISSUE");
        target.setEntityId(entity.getIssue().getId());
        target.setLabel(entity.getIssue().getProject().getKey() + "-" + entity.getIssue().getPosition());
        target.setRoute("/app/projects/" + entity.getIssue().getProject().getId() + "?issueId=" + entity.getIssue().getId());
        dto.setTarget(target);

        return dto;
    }

    private String buildMessage(IssueActivityEntity entity) {
        String actorName = entity.getActor().getFullName() != null
                ? entity.getActor().getFullName()
                : entity.getActor().getUsername();

        String issueKey = entity.getIssue().getProject().getKey() + "-" + entity.getIssue().getPosition();
        Map<String, String> payload = entity.getPayload();

        return switch (entity.getType()) {
            case ISSUE_CREATED -> actorName + " created " + issueKey;
            case ISSUE_UPDATED -> actorName + " updated " + issueKey;
            case STATUS_CHANGED -> actorName + " changed status of " + issueKey +
                    " from " + payload.getOrDefault("from", "—") +
                    " to " + payload.getOrDefault("to", "—");
            case ASSIGNEE_CHANGED -> actorName + " changed assignee of " + issueKey;
            case PRIORITY_CHANGED -> actorName + " changed priority of " + issueKey +
                    " from " + payload.getOrDefault("from", "—") +
                    " to " + payload.getOrDefault("to", "—");
            case LABELS_CHANGED -> actorName + " updated labels of " + issueKey;
            case DUE_DATE_CHANGED -> actorName + " updated due date of " + issueKey;
            case ISSUE_MOVED -> actorName + " moved " + issueKey +
                    " to " + payload.getOrDefault("to", "—");
            case COMMENT_ADDED -> actorName + " commented on " + issueKey;
        };
    }
}