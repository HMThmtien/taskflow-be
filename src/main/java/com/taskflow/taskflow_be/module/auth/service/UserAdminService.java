package com.taskflow.taskflow_be.module.auth.service;

public interface UserAdminService {
    void changeRole(String username, String role);
}