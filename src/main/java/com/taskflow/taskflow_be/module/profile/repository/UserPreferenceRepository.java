package com.taskflow.taskflow_be.module.profile.repository;

import com.taskflow.taskflow_be.module.profile.entity.UserPreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserPreferenceRepository extends JpaRepository<UserPreferenceEntity, UUID> {
}