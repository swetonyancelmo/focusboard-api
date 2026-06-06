package com.swetonyancelmo.focusboard.repository;

import com.swetonyancelmo.focusboard.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    Page<Task> findByUserId(UUID userId, Pageable pageable);
    Optional<Task> findByIdAndUserId(UUID taskId, UUID userId);

    @Modifying
    @Query("DELETE FROM Task t WHERE t.id = :taskId AND t.user.id = :userId")
    int deleteByIdAndUserId(UUID taskId, UUID userId);
}
