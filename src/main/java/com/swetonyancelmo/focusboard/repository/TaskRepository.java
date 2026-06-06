package com.swetonyancelmo.focusboard.repository;

import com.swetonyancelmo.focusboard.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
}
