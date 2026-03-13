package com.taskflow.taskflow_be.module.project.repository;

import com.taskflow.taskflow_be.module.project.entity.ProjectEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<ProjectEntity, UUID> {
    Optional<ProjectEntity> findByKey(String key);
    boolean existsByKey(String key);
    boolean existsByKeyAndIdNot(String key, UUID id);

    @Query("""
        select p from ProjectEntity p
        where p.id in :projectIds
          and (
            lower(p.name) like lower(concat('%', :q, '%'))
            or lower(p.key) like lower(concat('%', :q, '%'))
            or lower(coalesce(p.description, '')) like lower(concat('%', :q, '%'))
          )
        order by p.archived asc, p.createdAt desc
        """)
    List<ProjectEntity> searchVisibleProjects(
            @Param("projectIds") List<UUID> projectIds,
            @Param("q") String q,
            Pageable pageable
    );
}
