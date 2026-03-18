package com.taskflow.taskflow_be.module.workspace.service;

import com.taskflow.taskflow_be.common.util.SecurityUtils;
import com.taskflow.taskflow_be.exception.AppException;
import com.taskflow.taskflow_be.exception.ErrorCode;
import com.taskflow.taskflow_be.module.auth.entity.UserEntity;
import com.taskflow.taskflow_be.module.auth.repository.UserRepository;
import com.taskflow.taskflow_be.module.issue.entity.IssueEntity;
import com.taskflow.taskflow_be.module.issue.entity.IssuePriority;
import com.taskflow.taskflow_be.module.issue.entity.IssueStatus;
import com.taskflow.taskflow_be.module.issue.repository.IssueRepository;
import com.taskflow.taskflow_be.module.workspace.dto.MyWorkDtos;
import jakarta.persistence.criteria.Predicate;
import org.springframework.http.HttpStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MyWorkServiceImpl implements MyWorkService {

    private static final int MAX_QUERY_LENGTH = 100;

    private final IssueRepository issueRepository;
    private final UserRepository userRepository;

    public MyWorkServiceImpl(
            IssueRepository issueRepository,
            UserRepository userRepository
    ) {
        this.issueRepository = issueRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<MyWorkDtos.IssueSummaryResponse> getMyWork(
            String type,
            String q,
            UUID projectId,
            IssueStatus status,
            IssuePriority priority
    ) {
        UUID currentUserId = getCurrentUser().getId();
        String normalizedType = normalizeType(type);
        String normalizedQuery = normalizeOptionalQuery(q);

        Specification<IssueEntity> spec = (root, query, cb) -> {
            root.fetch("project");
            query.distinct(true);

            List<Predicate> predicates = new ArrayList<>();

            var projectJoin = root.join("project");
            var memberSubquery = query.subquery(UUID.class);
            var memberRoot = memberSubquery.from(com.taskflow.taskflow_be.module.project.entity.ProjectMemberEntity.class);

            memberSubquery.select(memberRoot.get("project").get("id"))
                    .where(cb.equal(memberRoot.get("user").get("id"), currentUserId));

            predicates.add(projectJoin.get("id").in(memberSubquery));

            if ("assigned".equalsIgnoreCase(normalizedType)) {
                predicates.add(cb.equal(root.get("assignee").get("id"), currentUserId));
            } else if ("created".equalsIgnoreCase(normalizedType)) {
                predicates.add(cb.equal(root.get("reporter").get("id"), currentUserId));
            } else if ("overdue".equalsIgnoreCase(normalizedType)) {
                predicates.add(cb.equal(root.get("assignee").get("id"), currentUserId));
                predicates.add(cb.lessThan(root.get("dueDate"), LocalDate.now()));
                predicates.add(cb.notEqual(root.get("status"), IssueStatus.DONE));
            } else {
                predicates.add(cb.equal(root.get("assignee").get("id"), currentUserId));
            }

            if (normalizedQuery != null) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + normalizedQuery.toLowerCase() + "%"));
            }
            if (projectId != null) {
                predicates.add(cb.equal(projectJoin.get("id"), projectId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }

            query.orderBy(cb.desc(root.get("updatedAt")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return issueRepository.findAll(spec).stream()
                .map(this::toResponse)
                .toList();
    }

    private UserEntity getCurrentUser() {
        var username = SecurityUtils.currentUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND, "User not found"));
    }

    private String normalizeType(String type) {
        String normalized = type == null ? "" : type.trim().toLowerCase();
        if (normalized.isBlank()) {
            throw new AppException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Type is required");
        }
        if (!List.of("assigned", "created", "overdue").contains(normalized)) {
            throw new AppException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Type must be assigned, created, or overdue");
        }
        return normalized;
    }

    private String normalizeOptionalQuery(String value) {
        if (value == null) return null;
        String normalized = value.trim().replaceAll("\\s+", " ");
        if (normalized.isBlank()) return null;
        if (normalized.length() > MAX_QUERY_LENGTH) {
            throw new AppException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Search query is too long");
        }
        return normalized;
    }

    private MyWorkDtos.IssueSummaryResponse toResponse(IssueEntity issue) {
        var dto = new MyWorkDtos.IssueSummaryResponse();
        dto.setId(issue.getId());
        dto.setKey(issue.getProject().getKey() + "-" + issue.getPosition());
        dto.setTitle(issue.getTitle());
        dto.setDescription(issue.getDescription());
        dto.setStatus(issue.getStatus());
        dto.setPriority(issue.getPriority());
        dto.setDueDate(issue.getDueDate());
        dto.setUpdatedAt(issue.getUpdatedAt());
        dto.setProjectId(issue.getProject().getId());
        dto.setProjectKey(issue.getProject().getKey());
        dto.setProjectName(issue.getProject().getName());
        dto.setLabels(issue.getLabels());

        if (issue.getAssignee() != null) {
            var assignee = new MyWorkDtos.SimpleUserResponse();
            assignee.setId(issue.getAssignee().getId());
            assignee.setUsername(issue.getAssignee().getUsername());
            assignee.setFullName(issue.getAssignee().getFullName());
            dto.setAssignee(assignee);
        }

        return dto;
    }
}
