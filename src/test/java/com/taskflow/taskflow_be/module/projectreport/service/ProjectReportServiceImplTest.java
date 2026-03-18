package com.taskflow.taskflow_be.module.projectreport.service;

import com.taskflow.taskflow_be.module.issue.entity.IssueStatus;
import com.taskflow.taskflow_be.module.issue.repository.IssueRepository;
import com.taskflow.taskflow_be.module.project.repository.ProjectRepository;
import com.taskflow.taskflow_be.module.project.service.ProjectPermissionService;
import com.taskflow.taskflow_be.module.sprint.repository.SprintRepository;
import com.taskflow.taskflow_be.common.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectReportServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectPermissionService permissionService;

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private SprintRepository sprintRepository;

    private ProjectReportServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProjectReportServiceImpl(projectRepository, permissionService, issueRepository, sprintRepository);
    }

    @Test
    void getSummaryBuildsTotalsFromAggregates() {
        UUID projectId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();

        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::currentUserId).thenReturn(currentUserId);

            doNothing().when(permissionService).requireMember(projectId, currentUserId);
            when(projectRepository.findById(projectId)).thenReturn(Optional.of(mock(com.taskflow.taskflow_be.module.project.entity.ProjectEntity.class)));
            when(issueRepository.countByProject_Id(projectId)).thenReturn(10L);
            when(issueRepository.countByProject_IdAndStatus(projectId, IssueStatus.DONE)).thenReturn(4L);
            when(issueRepository.countByProject_IdAndDueDateBeforeAndStatusNot(any(), any(LocalDate.class), any())).thenReturn(2L);
            when(issueRepository.countByStatusForProject(projectId)).thenReturn(List.of());
            when(issueRepository.countByPriorityForProject(projectId)).thenReturn(List.of());
            when(issueRepository.findOverdueIssuesForProject(any(), any(LocalDate.class), any())).thenReturn(List.of());
            when(sprintRepository.findByProject_IdAndStatus(any(), any())).thenReturn(Optional.empty());

            var result = service.getSummary(projectId);

            assertEquals(10L, result.getTotalIssues());
            assertEquals(4L, result.getDoneIssues());
            assertEquals(6L, result.getOpenIssues());
            assertEquals(2L, result.getOverdueIssues());
        }
    }
}
