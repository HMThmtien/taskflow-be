package com.taskflow.taskflow_be.module.issue.repository;

import com.taskflow.taskflow_be.module.issue.entity.CommentMentionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentMentionRepository extends JpaRepository<CommentMentionEntity, UUID> {

    boolean existsByComment_IdAndMentionedUser_Id(UUID commentId, UUID mentionedUserId);

    List<CommentMentionEntity> findByComment_Id(UUID commentId);
}