package com.taskflow.taskflow_be.module.issue.service;

import com.taskflow.taskflow_be.common.util.SecurityUtils;
import com.taskflow.taskflow_be.exception.ErrorCode;
import com.taskflow.taskflow_be.exception.NotFoundException;
import com.taskflow.taskflow_be.module.auth.repository.UserRepository;
import com.taskflow.taskflow_be.module.issue.entity.IssueActivityType;
import com.taskflow.taskflow_be.module.issue.repository.IssueRepository;
import com.taskflow.taskflow_be.module.issue.dto.IssueCommentDtos;
import com.taskflow.taskflow_be.module.issue.entity.IssueCommentEntity;
import com.taskflow.taskflow_be.module.issue.repository.IssueCommentRepository;
import com.taskflow.taskflow_be.module.project.service.ProjectPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class IssueCommentServiceImpl implements IssueCommentService {

    private final IssueCommentRepository commentRepo;
    private final IssueRepository issueRepo;
    private final UserRepository userRepo;
    private final ProjectPermissionService permission;
    private final IssueActivityService activityService;

    @Override
    @Transactional(readOnly = true)
    public Page<IssueCommentDtos.CommentRes> list(UUID issueId, Pageable pageable) {
        UUID me = SecurityUtils.currentUserId();

        var issue = issueRepo.findById(issueId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ISSUE_NOT_FOUND, "Issue not found"));

        permission.requireMember(issue.getProject().getId(), me);

        return commentRepo.findByIssue_IdOrderByCreatedAtDesc(issueId, pageable)
                .map(c -> IssueCommentDtos.CommentRes.builder()
                        .id(c.getId())
                        .issueId(c.getIssue().getId())
                        .authorId(c.getAuthor().getId())
                        .authorUsername(c.getAuthor().getUsername())
                        .content(c.getContent())
                        .createdAt(c.getCreatedAt())
                        .build());
    }

    @Override
    public IssueCommentDtos.CommentRes create(UUID issueId, IssueCommentDtos.CreateReq req) {
        UUID me = SecurityUtils.currentUserId();

        var issue = issueRepo.findById(issueId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ISSUE_NOT_FOUND, "Issue not found"));

        permission.requireMember(issue.getProject().getId(), me);

        var author = userRepo.findById(me)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND, "User not found"));

        var entity = IssueCommentEntity.builder()
                .issue(issue)
                .author(author)
                .content(req.getContent().trim())
                .build();

        var saved = commentRepo.save(entity);

        // ✅ log activity
        activityService.log(
                issue,
                me,
                IssueActivityType.COMMENT_ADDED,
                Map.of("commentId", saved.getId().toString())
        );

        return IssueCommentDtos.CommentRes.builder()
                .id(saved.getId())
                .issueId(issue.getId())
                .authorId(author.getId())
                .authorUsername(author.getUsername())
                .content(saved.getContent())
                .createdAt(saved.getCreatedAt())
                .build();
    }
}