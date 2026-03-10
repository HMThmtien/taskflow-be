package com.taskflow.taskflow_be.module.issue.service;

import com.taskflow.taskflow_be.exception.ErrorCode;
import com.taskflow.taskflow_be.exception.NotFoundException;
import com.taskflow.taskflow_be.module.auth.repository.UserRepository;
import com.taskflow.taskflow_be.module.issue.dto.IssueActivityDtos;
import com.taskflow.taskflow_be.module.issue.entity.IssueActivityEntity;
import com.taskflow.taskflow_be.module.issue.entity.IssueActivityType;
import com.taskflow.taskflow_be.module.issue.entity.IssueEntity;
import com.taskflow.taskflow_be.module.issue.repository.IssueActivityRepository;
import com.taskflow.taskflow_be.module.issue.repository.IssueRepository;
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

        activityRepo.save(e);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IssueActivityDtos.ActivityRes> list(UUID issueId) {
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