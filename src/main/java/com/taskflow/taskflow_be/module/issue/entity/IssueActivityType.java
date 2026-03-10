package com.taskflow.taskflow_be.module.issue.entity;

public enum IssueActivityType {
    ISSUE_CREATED,
    ISSUE_UPDATED,
    STATUS_CHANGED,
    ASSIGNEE_CHANGED,
    PRIORITY_CHANGED,
    LABELS_CHANGED,
    DUE_DATE_CHANGED,
    ISSUE_MOVED,
    COMMENT_ADDED
}