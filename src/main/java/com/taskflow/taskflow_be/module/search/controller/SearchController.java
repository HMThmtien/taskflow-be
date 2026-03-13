package com.taskflow.taskflow_be.module.search.controller;

import com.taskflow.taskflow_be.module.search.dto.SearchDtos;
import com.taskflow.taskflow_be.module.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public SearchDtos.SearchResponse search(
            @RequestParam String q,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return searchService.search(q, limit);
    }
}
