package com.taskflow.taskflow_be.module.issue.service;

import com.taskflow.taskflow_be.exception.ErrorCode;
import com.taskflow.taskflow_be.exception.NotFoundException;
import com.taskflow.taskflow_be.module.issue.dto.IssueDtos;
import com.taskflow.taskflow_be.module.issue.entity.IssueEntity;
import com.taskflow.taskflow_be.module.issue.entity.IssuePriority;
import com.taskflow.taskflow_be.module.issue.entity.IssueStatus;
import com.taskflow.taskflow_be.module.issue.mapper.IssueMapper;
import com.taskflow.taskflow_be.module.issue.repository.IssueRepository;
import com.taskflow.taskflow_be.module.issue.repository.IssueSpecs;
import com.taskflow.taskflow_be.module.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class IssueServiceImpl implements IssueService {

    private final IssueRepository issueRepo;
    private final ProjectRepository projectRepo;

    @Override
    @Transactional(readOnly = true)
    public List<IssueDtos.IssueResponse> list(UUID projectId, String q, IssueStatus status, IssuePriority priority) {
        projectRepo.findById(projectId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PROJECT_NOT_FOUND, "Project not found"));

        Specification<IssueEntity> spec = IssueSpecs.projectId(projectId);

        if (q != null && !q.trim().isBlank()) {
            spec = spec.and(IssueSpecs.titleContains(q.trim()));
        }
        if (status != null) spec = spec.and(IssueSpecs.status(status));
        if (priority != null) spec = spec.and(IssueSpecs.priority(priority));

        return issueRepo.findAll(spec).stream().map(IssueMapper::toResponse).toList();
    }

    @Override
    public IssueDtos.IssueResponse create(UUID projectId, IssueDtos.CreateIssueRequest req) {
        var project = projectRepo.findById(projectId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PROJECT_NOT_FOUND, "Project not found"));

        IssueEntity e = IssueEntity.builder()
                .project(project)
                .title(req.getTitle().trim())
                .description(req.getDescription())
                .status(req.getStatus())       // đảm bảo req.getStatus() là IssueStatus
                .priority(req.getPriority())   // đảm bảo req.getPriority() là IssuePriority
                .build();

        return IssueMapper.toResponse(issueRepo.save(e));
    }

    @Override
    public IssueDtos.IssueResponse update(UUID projectId, UUID issueId, IssueDtos.UpdateIssueRequest req) {
        var issue = issueRepo.findById(issueId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ISSUE_NOT_FOUND, "Issue not found"));

        if (!issue.getProject().getId().equals(projectId)) {
            throw new NotFoundException(ErrorCode.ISSUE_NOT_FOUND, "Issue not found");
        }

        issue.setTitle(req.getTitle().trim());
        issue.setDescription(req.getDescription());
        if (req.getStatus() != null) issue.setStatus(req.getStatus());
        if (req.getPriority() != null) issue.setPriority(req.getPriority());

        return IssueMapper.toResponse(issue);
    }

    @Override
    public IssueDtos.IssueResponse move(UUID projectId, UUID issueId, IssueDtos.MoveIssueRequest req) {
        var issue = issueRepo.findById(issueId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ISSUE_NOT_FOUND, "Issue not found"));

        if (!issue.getProject().getId().equals(projectId)) {
            throw new NotFoundException(ErrorCode.ISSUE_NOT_FOUND, "Issue not found");
        }

        if (req.getStatus() == null) {
            // nên dùng AppException thay vì IllegalArgumentException để trả JSON chuẩn
            throw new com.taskflow.taskflow_be.exception.AppException(
                    ErrorCode.BAD_REQUEST,
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "status is required"
            );
        }

        issue.setStatus(req.getStatus());
        return IssueMapper.toResponse(issue);
    }
}