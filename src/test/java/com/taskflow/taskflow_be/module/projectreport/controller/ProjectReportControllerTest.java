package com.taskflow.taskflow_be.module.projectreport.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.taskflow_be.module.projectreport.dto.ProjectReportDtos;
import com.taskflow.taskflow_be.module.projectreport.service.ProjectReportService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectReportService projectReportService;

    @Test
    void summaryReturnsWrappedPayload() throws Exception {
        UUID projectId = UUID.randomUUID();
        when(projectReportService.getSummary(projectId)).thenReturn(
                ProjectReportDtos.ProjectSummaryResponse.builder()
                        .projectId(projectId)
                        .totalIssues(12)
                        .openIssues(8)
                        .doneIssues(4)
                        .overdueIssues(2)
                        .issuesByStatus(List.of())
                        .issuesByPriority(List.of())
                        .overdueItems(List.of())
                        .activeSprint(null)
                        .build()
        );

        mockMvc.perform(get("/api/projects/{projectId}/reports/summary", projectId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalIssues").value(12));
    }
}
