package com.taskflow.taskflow_be.module.sprint.controller;

import com.taskflow.taskflow_be.module.issue.dto.IssueDtos;
import com.taskflow.taskflow_be.module.sprint.dto.SprintDtos;
import com.taskflow.taskflow_be.module.sprint.service.SprintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}")
public class SprintController {

    private final SprintService sprintService;

    @GetMapping("/sprints")
    public List<SprintDtos.SprintResponse> list(@PathVariable UUID projectId) {
        return sprintService.list(projectId);
    }

    @PostMapping("/sprints")
    public SprintDtos.SprintResponse create(
            @PathVariable UUID projectId,
            @Valid @RequestBody SprintDtos.CreateSprintRequest req
    ) {
        return sprintService.create(projectId, req);
    }

    @PutMapping("/sprints/{sprintId}")
    public SprintDtos.SprintResponse update(
            @PathVariable UUID projectId,
            @PathVariable UUID sprintId,
            @Valid @RequestBody SprintDtos.UpdateSprintRequest req
    ) {
        return sprintService.update(projectId, sprintId, req);
    }

    @PatchMapping("/sprints/{sprintId}/start")
    public SprintDtos.SprintResponse start(
            @PathVariable UUID projectId,
            @PathVariable UUID sprintId
    ) {
        return sprintService.start(projectId, sprintId);
    }

    @PatchMapping("/sprints/{sprintId}/complete")
    public SprintDtos.SprintResponse complete(
            @PathVariable UUID projectId,
            @PathVariable UUID sprintId
    ) {
        return sprintService.complete(projectId, sprintId);
    }

    @GetMapping("/backlog/issues")
    public List<IssueDtos.IssueResponse> backlog(@PathVariable UUID projectId) {
        return sprintService.listBacklogIssues(projectId);
    }

    @PatchMapping("/issues/{issueId}/sprint")
    public IssueDtos.IssueResponse assignIssue(
            @PathVariable UUID projectId,
            @PathVariable UUID issueId,
            @RequestBody(required = false) SprintDtos.AssignIssueSprintRequest req
    ) {
        return sprintService.assignIssue(projectId, issueId, req == null ? null : req.getSprintId());
    }
}
