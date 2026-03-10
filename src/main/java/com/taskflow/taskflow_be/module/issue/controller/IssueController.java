package com.taskflow.taskflow_be.module.issue.controller;

import com.taskflow.taskflow_be.module.issue.dto.IssueDtos;
import com.taskflow.taskflow_be.module.issue.entity.IssuePriority;
import com.taskflow.taskflow_be.module.issue.entity.IssueStatus;
import com.taskflow.taskflow_be.module.issue.service.IssueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}/issues")
public class IssueController {

    private final IssueService service;

    @GetMapping
    public Page<IssueDtos.IssueResponse> list(
            @PathVariable UUID projectId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) IssueStatus status,
            @RequestParam(required = false) IssuePriority priority,
            @RequestParam(required = false) UUID assigneeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueTo,
            @RequestParam(required = false) String label,
            Pageable pageable
    ) {
        return service.list(
                projectId,
                q,
                status,
                priority,
                assigneeId,
                dueFrom,
                dueTo,
                label,
                pageable
        );
    }

    @PostMapping
    public IssueDtos.IssueResponse create(
            @PathVariable UUID projectId,
            @Valid @RequestBody IssueDtos.CreateIssueRequest req
    ) {
        return service.create(projectId, req);
    }

    @PutMapping("/{issueId}")
    public IssueDtos.IssueResponse update(
            @PathVariable UUID projectId,
            @PathVariable UUID issueId,
            @RequestBody IssueDtos.UpdateIssueRequest req // ✅ bỏ @Valid để partial update không bị fail
    ) {
        return service.update(projectId, issueId, req);
    }

    @PatchMapping("/{issueId}/move")
    public IssueDtos.IssueResponse move(
            @PathVariable UUID projectId,
            @PathVariable UUID issueId,
            @RequestBody IssueDtos.MoveIssueRequest req
    ) {
        return service.move(projectId, issueId, req);
    }
}