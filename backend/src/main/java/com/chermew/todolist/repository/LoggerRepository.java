package com.chermew.todolist.repository;

import com.chermew.todolist.entity.Logger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LoggerRepository extends JpaRepository<Logger, UUID> {
}
