package com.swetonyancelmo.focusboard.repository;

import com.swetonyancelmo.focusboard.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findByUserId(UUID userId);
    Optional<Task> findByIdAndUserId(UUID taskId, UUID userId);

    @Modifying
    @Query("DELETE FROM Task t WHERE t.id = :taskId AND t.user.id = :userId")
    int deleteByIdAndUserId(UUID taskId, UUID userId);
}
