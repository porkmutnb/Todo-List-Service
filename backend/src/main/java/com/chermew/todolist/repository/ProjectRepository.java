package com.chermew.todolist.repository;

import com.chermew.todolist.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN ProjectMember pm ON pm.project.id = p.id " +
           "WHERE p.ownerId = :userId OR pm.userId = :userId")
    List<Project> findAllByOwnerOrMember(@Param("userId") UUID userId);
}
