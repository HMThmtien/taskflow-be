package com.taskflow.taskflow_be.module.issue.service;

import com.taskflow.taskflow_be.exception.AppException;
import com.taskflow.taskflow_be.exception.ErrorCode;
import com.taskflow.taskflow_be.module.auth.entity.UserEntity;
import com.taskflow.taskflow_be.module.auth.repository.UserRepository;
import com.taskflow.taskflow_be.module.issue.entity.IssueActivityEntity;
import com.taskflow.taskflow_be.module.issue.entity.IssueActivityType;
import com.taskflow.taskflow_be.module.issue.entity.IssueEntity;
import com.taskflow.taskflow_be.module.issue.repository.IssueActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IssueActivityLogService {

    private final IssueActivityRepository activityRepo;
    private final UserRepository userRepo;

    public void log(UUID actorId, IssueEntity issue, IssueActivityType type, Map<String, String> payload) {
        UserEntity actor = userRepo.findById(actorId)
                .orElseThrow(() -> new AppException(
                        ErrorCode.USER_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        activityRepo.save(IssueActivityEntity.builder()
                .issue(issue)
                .actor(actor)
                .type(type)
                .payload(payload == null ? Map.of() : payload)
                .build());
    }
}