package com.taskflow.taskflow_be.module.search.service;

import com.taskflow.taskflow_be.module.search.dto.SearchDtos;

public interface SearchService {
    SearchDtos.SearchResponse search(String q, int limit);
}
