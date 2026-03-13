package com.taskflow.taskflow_be.module.issue.service;

import com.taskflow.taskflow_be.exception.ErrorCode;
import com.taskflow.taskflow_be.exception.NotFoundException;
import com.taskflow.taskflow_be.common.util.SecurityUtils;
import com.taskflow.taskflow_be.module.auth.repository.UserRepository;
import com.taskflow.taskflow_be.module.issue.dto.IssueActivityDtos;
import com.taskflow.taskflow_be.module.issue.entity.IssueActivityEntity;
import com.taskflow.taskflow_be.module.issue.entity.IssueActivityType;
import com.taskflow.taskflow_be.module.issue.entity.IssueEntity;
import com.taskflow.taskflow_be.module.issue.repository.IssueActivityRepository;
import com.taskflow.taskflow_be.module.issue.repository.IssueRepository;
import com.taskflow.taskflow_be.module.project.repository.ProjectMemberRepository;
import com.taskflow.taskflow_be.module.project.service.ProjectPermissionService;
import com.taskflow.taskflow_be.module.realtime.dto.RealtimeDtos;
import com.taskflow.taskflow_be.module.realtime.service.RealtimeEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class IssueActivityServiceImpl implements IssueActivityService {

    private final IssueActivityRepository activityRepo;
    private final IssueRepository issueRepo;
    private final UserRepository userRepo;
    private final ProjectMemberRepository projectMemberRepo;
    private final ProjectPermissionService permission;
    private final RealtimeEventService realtimeEventService;

    @Override
    public void log(IssueEntity issue, UUID actorId, IssueActivityType type, Map<String, String> payload) {
        var actor = userRepo.findById(actorId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND, "User not found"));

        var e = IssueActivityEntity.builder()
                .issue(issue)
                .actor(actor)
                .type(type)
                .payload(payload == null ? Map.of() : payload)
                .build();

        var saved = activityRepo.save(e);
        var payloadDto = IssueActivityDtos.ActivityRes.builder()
                .id(saved.getId())
                .issueId(saved.getIssue().getId())
                .actorId(saved.getActor().getId())
                .actorUsername(saved.getActor().getUsername())
                .type(saved.getType().name())
                .payload(saved.getPayload() == null ? new java.util.HashMap<>() : new java.util.HashMap<>(saved.getPayload()))
                .createdAt(saved.getCreatedAt())
                .build();

        var memberIds = projectMemberRepo.findAllByProjectId(issue.getProject().getId()).stream()
                .map(member -> member.getUser().getId())
                .toList();

        realtimeEventService.publishToUsers(
                memberIds,
                "issue.activity.created",
                RealtimeDtos.IssueActivityEventPayload.builder()
                        .issueId(issue.getId())
                        .activity(payloadDto)
                        .build()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<IssueActivityDtos.ActivityRes> list(UUID issueId) {
        UUID currentUserId = SecurityUtils.currentUserId();
        var issue = issueRepo.findById(issueId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ISSUE_NOT_FOUND, "Issue not found"));
        permission.requireMember(issue.getProject().getId(), currentUserId);

        return activityRepo.findAllByIssue_IdOrderByCreatedAtAsc(issueId).stream().map(a ->
                IssueActivityDtos.ActivityRes.builder()
                        .id(a.getId())
                        .issueId(a.getIssue().getId())
                        .actorId(a.getActor().getId())
                        .actorUsername(a.getActor().getUsername())
                        .type(a.getType().name())
                        .payload(a.getPayload() == null ? new java.util.HashMap<>() : new java.util.HashMap<>(a.getPayload()))
                        .createdAt(a.getCreatedAt())
                        .build()
        ).toList();
    }
}
