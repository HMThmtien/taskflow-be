package com.taskflow.taskflow_be.module.issue.service;

import com.taskflow.taskflow_be.common.util.SecurityUtils;
import com.taskflow.taskflow_be.exception.AppException;
import com.taskflow.taskflow_be.exception.ErrorCode;
import com.taskflow.taskflow_be.exception.NotFoundException;
import com.taskflow.taskflow_be.module.auth.entity.UserEntity;
import com.taskflow.taskflow_be.module.auth.repository.UserRepository;
import com.taskflow.taskflow_be.module.issue.dto.IssueCommentDtos;
import com.taskflow.taskflow_be.module.issue.entity.CommentMentionEntity;
import com.taskflow.taskflow_be.module.issue.entity.IssueActivityType;
import com.taskflow.taskflow_be.module.issue.entity.IssueCommentEntity;
import com.taskflow.taskflow_be.module.issue.repository.CommentMentionRepository;
import com.taskflow.taskflow_be.module.issue.repository.IssueCommentRepository;
import com.taskflow.taskflow_be.module.issue.repository.IssueRepository;
import com.taskflow.taskflow_be.module.notification.entity.NotificationEntity;
import com.taskflow.taskflow_be.module.notification.repository.NotificationRepository;
import com.taskflow.taskflow_be.module.notification.service.NotificationRealtimeService;
import com.taskflow.taskflow_be.module.project.repository.ProjectMemberRepository;
import com.taskflow.taskflow_be.module.project.service.ProjectPermissionService;
import com.taskflow.taskflow_be.module.realtime.dto.RealtimeDtos;
import com.taskflow.taskflow_be.module.realtime.service.RealtimeEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional
public class IssueCommentServiceImpl implements IssueCommentService {

    private static final Pattern MENTION_PATTERN = Pattern.compile("@([a-zA-Z0-9._-]+)");

    private final IssueCommentRepository commentRepo;
    private final CommentMentionRepository commentMentionRepo;
    private final IssueRepository issueRepo;
    private final UserRepository userRepo;
    private final ProjectMemberRepository projectMemberRepo;
    private final NotificationRepository notificationRepo;
    private final ProjectPermissionService permission;
    private final IssueActivityService activityService;
    private final NotificationRealtimeService notificationRealtimeService;
    private final RealtimeEventService realtimeEventService;

    @Override
    @Transactional(readOnly = true)
    public Page<IssueCommentDtos.CommentRes> list(UUID issueId, Pageable pageable) {
        UUID me = SecurityUtils.currentUserId();

        var issue = issueRepo.findById(issueId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ISSUE_NOT_FOUND, "Issue not found"));

        permission.requireMember(issue.getProject().getId(), me);

        return commentRepo.findByIssue_IdOrderByCreatedAtDesc(issueId, pageable)
                .map(this::toResponse);
    }

    @Override
    public IssueCommentDtos.CommentRes create(UUID issueId, IssueCommentDtos.CreateReq req) {
        UUID me = SecurityUtils.currentUserId();

        var issue = issueRepo.findById(issueId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ISSUE_NOT_FOUND, "Issue not found"));

        permission.requireMember(issue.getProject().getId(), me);

        var author = userRepo.findById(me)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND, "User not found"));

        String content = req.getContent() == null ? null : req.getContent().trim();
        if (content == null || content.isBlank()) {
            throw new AppException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Comment content must not be blank");
        }

        var entity = IssueCommentEntity.builder()
                .issue(issue)
                .author(author)
                .content(content)
                .build();

        var saved = commentRepo.save(entity);

        List<UserEntity> mentionedUsers = resolveMentionedUsers(content, issue.getProject().getId(), author.getId());
        Set<UUID> notifiedUserIds = new HashSet<>();

        for (UserEntity mentionedUser : mentionedUsers) {
            if (!commentMentionRepo.existsByComment_IdAndMentionedUser_Id(saved.getId(), mentionedUser.getId())) {
                commentMentionRepo.save(
                        CommentMentionEntity.builder()
                                .comment(saved)
                                .mentionedUser(mentionedUser)
                                .build()
                );
            }

            var notification = notificationRepo.save(
                    NotificationEntity.builder()
                            .user(mentionedUser)
                            .actor(author)
                            .type("MENTIONED")
                            .title("You were mentioned in " + issue.getProject().getKey() + "-" + issue.getPosition())
                            .body(author.getUsername() + " mentioned you in a comment on " + issue.getTitle())
                            .entityType("ISSUE")
                            .entityId(issue.getId())
                            .route("/app/projects/" + issue.getProject().getId() + "?issueId=" + issue.getId())
                            .isRead(false)
                            .build()
            );
            notificationRealtimeService.publishCreated(notification);
            notifiedUserIds.add(mentionedUser.getId());
        }

        notifyCommentStakeholders(issue, author, notifiedUserIds);

        activityService.log(
                issue,
                me,
                IssueActivityType.COMMENT_ADDED,
                Map.of("commentId", saved.getId().toString())
        );

        var response = toResponse(saved, mentionedUsers);

        var memberIds = projectMemberRepo.findAllByProjectId(issue.getProject().getId()).stream()
                .map(member -> member.getUser().getId())
                .toList();

        realtimeEventService.publishToUsers(
                memberIds,
                "issue.comment.created",
                RealtimeDtos.IssueCommentEventPayload.builder()
                        .issueId(issue.getId())
                        .comment(response)
                        .build()
        );

        return response;
    }

    private void notifyCommentStakeholders(
            com.taskflow.taskflow_be.module.issue.entity.IssueEntity issue,
            UserEntity author,
            Set<UUID> excludedUserIds
    ) {
        Set<UserEntity> recipients = new LinkedHashSet<>();

        if (issue.getReporter() != null) {
            recipients.add(issue.getReporter());
        }

        if (issue.getAssignee() != null) {
            recipients.add(issue.getAssignee());
        }

        for (UserEntity recipient : recipients) {
            if (recipient == null) continue;
            if (recipient.getId().equals(author.getId())) continue;
            if (excludedUserIds.contains(recipient.getId())) continue;

            var notification = notificationRepo.save(
                    NotificationEntity.builder()
                            .user(recipient)
                            .actor(author)
                            .type("ISSUE_COMMENTED")
                            .title("New comment on " + issue.getProject().getKey() + "-" + issue.getPosition())
                            .body(author.getUsername() + " commented on " + issue.getTitle())
                            .entityType("ISSUE")
                            .entityId(issue.getId())
                            .route("/app/projects/" + issue.getProject().getId() + "?issueId=" + issue.getId())
                            .isRead(false)
                            .build()
            );
            notificationRealtimeService.publishCreated(notification);
        }
    }

    private List<UserEntity> resolveMentionedUsers(String content, UUID projectId, UUID authorId) {
        Set<String> usernames = new LinkedHashSet<>();
        Matcher matcher = MENTION_PATTERN.matcher(content);

        while (matcher.find()) {
            String username = matcher.group(1);
            if (username != null && !username.isBlank()) {
                usernames.add(username.trim());
            }
        }

        if (usernames.isEmpty()) {
            return List.of();
        }

        List<UserEntity> result = new ArrayList<>();

        for (String username : usernames) {
            userRepo.findByUsername(username).ifPresent(user -> {
                if (user.getId().equals(authorId)) return;

                boolean isMember = projectMemberRepo.existsByProjectIdAndUserId(projectId, user.getId());
                if (isMember) {
                    result.add(user);
                }
            });
        }

        return result;
    }

    private IssueCommentDtos.CommentRes toResponse(IssueCommentEntity c) {
        List<IssueCommentDtos.MentionedUserResponse> mentions =
                commentMentionRepo.findByComment_Id(c.getId()).stream()
                        .map(m -> IssueCommentDtos.MentionedUserResponse.builder()
                                .id(m.getMentionedUser().getId())
                                .username(m.getMentionedUser().getUsername())
                                .fullName(m.getMentionedUser().getFullName())
                                .build())
                        .toList();

        return IssueCommentDtos.CommentRes.builder()
                .id(c.getId())
                .issueId(c.getIssue().getId())
                .authorId(c.getAuthor().getId())
                .authorUsername(c.getAuthor().getUsername())
                .authorFullName(c.getAuthor().getFullName())
                .content(c.getContent())
                .createdAt(c.getCreatedAt())
                .mentions(mentions)
                .build();
    }

    private IssueCommentDtos.CommentRes toResponse(IssueCommentEntity c, List<UserEntity> mentionedUsers) {
        return IssueCommentDtos.CommentRes.builder()
                .id(c.getId())
                .issueId(c.getIssue().getId())
                .authorId(c.getAuthor().getId())
                .authorUsername(c.getAuthor().getUsername())
                .authorFullName(c.getAuthor().getFullName())
                .content(c.getContent())
                .createdAt(c.getCreatedAt())
                .mentions(
                        mentionedUsers.stream()
                                .map(u -> IssueCommentDtos.MentionedUserResponse.builder()
                                        .id(u.getId())
                                        .username(u.getUsername())
                                        .fullName(u.getFullName())
                                        .build())
                                .toList()
                )
                .build();
    }
}
