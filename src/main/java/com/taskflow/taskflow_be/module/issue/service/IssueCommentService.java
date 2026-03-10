package com.taskflow.taskflow_be.module.issue.service;

import com.taskflow.taskflow_be.module.issue.dto.IssueCommentDtos;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IssueCommentService {
    Page<IssueCommentDtos.CommentRes> list(UUID issueId, Pageable pageable);
    IssueCommentDtos.CommentRes create(UUID issueId, IssueCommentDtos.CreateReq req);
}