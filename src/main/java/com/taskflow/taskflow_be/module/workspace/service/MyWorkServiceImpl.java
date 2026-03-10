package com.taskflow.taskflow_be.module.workspace.service;

import com.taskflow.taskflow_be.common.util.SecurityUtils;
import com.taskflow.taskflow_be.module.auth.entity.UserEntity;
import com.taskflow.taskflow_be.module.auth.repository.UserRepository;
import com.taskflow.taskflow_be.module.issue.entity.IssueEntity;
import com.taskflow.taskflow_be.module.issue.entity.IssuePriority;
import com.taskflow.taskflow_be.module.issue.entity.IssueStatus;
import com.taskflow.taskflow_be.module.issue.repository.IssueRepository;
import com.taskflow.taskflow_be.module.workspace.dto.MyWorkDtos;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MyWorkServiceImpl implements MyWorkService {

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

            if ("assigned".equalsIgnoreCase(type)) {
                predicates.add(cb.equal(root.get("assignee").get("id"), currentUserId));
            } else if ("created".equalsIgnoreCase(type)) {
                predicates.add(cb.equal(root.get("reporter").get("id"), currentUserId));
            } else if ("overdue".equalsIgnoreCase(type)) {
                predicates.add(cb.equal(root.get("assignee").get("id"), currentUserId));
                predicates.add(cb.lessThan(root.get("dueDate"), LocalDate.now()));
                predicates.add(cb.notEqual(root.get("status"), IssueStatus.DONE));
            } else {
                predicates.add(cb.equal(root.get("assignee").get("id"), currentUserId));
            }

            if (q != null && !q.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + q.trim().toLowerCase() + "%"));
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
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
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