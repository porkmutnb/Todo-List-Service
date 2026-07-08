package com.chermew.todolist.repository;

import com.chermew.todolist.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findByProjectId(UUID projectId);
    boolean existsByProjectIdAndName(UUID projectId, String name);
}
