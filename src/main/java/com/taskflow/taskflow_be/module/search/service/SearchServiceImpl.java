package com.taskflow.taskflow_be.module.search.service;

import com.taskflow.taskflow_be.common.util.SecurityUtils;
import com.taskflow.taskflow_be.module.auth.entity.GlobalRole;
import com.taskflow.taskflow_be.module.auth.entity.UserEntity;
import com.taskflow.taskflow_be.module.auth.repository.UserRepository;
import com.taskflow.taskflow_be.module.issue.repository.IssueRepository;
import com.taskflow.taskflow_be.module.project.entity.ProjectMemberEntity;
import com.taskflow.taskflow_be.module.project.repository.ProjectMemberRepository;
import com.taskflow.taskflow_be.module.project.repository.ProjectRepository;
import com.taskflow.taskflow_be.module.search.dto.SearchDtos;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchServiceImpl implements SearchService {

    private final ProjectRepository projectRepository;
    private final IssueRepository issueRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;

    @Override
    public SearchDtos.SearchResponse search(String q, int limit) {
        String query = q == null ? "" : q.trim();
        if (query.isBlank()) {
            return SearchDtos.SearchResponse.builder()
                    .projects(List.of())
                    .issues(List.of())
                    .users(List.of())
                    .build();
        }

        int safeLimit = Math.max(1, Math.min(limit, 10));
        UUID currentUserId = SecurityUtils.currentUserId();
        UserEntity currentUser = userRepository.findById(currentUserId).orElseThrow();
        boolean isAdmin = currentUser.getRole() == GlobalRole.ADMIN;

        List<UUID> visibleProjectIds = projectMemberRepository.findAllByUserId(currentUserId).stream()
                .map(ProjectMemberEntity::getProject)
                .map(project -> project.getId())
                .distinct()
                .toList();

        PageRequest pageable = PageRequest.of(0, safeLimit);

        var projects = visibleProjectIds.isEmpty()
                ? List.<SearchDtos.ProjectResult>of()
                : projectRepository.searchVisibleProjects(visibleProjectIds, query, pageable).stream()
                .map(project -> SearchDtos.ProjectResult.builder()
                        .id(project.getId())
                        .key(project.getKey())
                        .name(project.getName())
                        .description(project.getDescription())
                        .archived(project.isArchived())
                        .build())
                .toList();

        var issues = visibleProjectIds.isEmpty()
                ? List.<SearchDtos.IssueResult>of()
                : issueRepository.searchVisibleIssues(visibleProjectIds, query, pageable).stream()
                .map(issue -> SearchDtos.IssueResult.builder()
                        .id(issue.getId())
                        .projectId(issue.getProject().getId())
                        .projectKey(issue.getProject().getKey())
                        .projectName(issue.getProject().getName())
                        .title(issue.getTitle())
                        .status(issue.getStatus())
                        .priority(issue.getPriority())
                        .type(issue.getType())
                        .build())
                .toList();

        var users = searchUsers(currentUserId, visibleProjectIds, query, pageable, isAdmin).stream()
                .map(user -> SearchDtos.UserResult.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .fullName(user.getFullName())
                        .role(user.getRole())
                        .build())
                .toList();

        return SearchDtos.SearchResponse.builder()
                .projects(projects)
                .issues(issues)
                .users(users)
                .build();
    }

    private List<UserEntity> searchUsers(
            UUID currentUserId,
            List<UUID> visibleProjectIds,
            String query,
            PageRequest pageable,
            boolean isAdmin
    ) {
        if (isAdmin) {
            return userRepository.searchAllUsers(query, pageable);
        }
        if (visibleProjectIds.isEmpty()) {
            return userRepository.searchSelf(currentUserId, query, pageable);
        }
        return userRepository.searchWorkspaceUsers(currentUserId, visibleProjectIds, query, pageable);
    }
}
