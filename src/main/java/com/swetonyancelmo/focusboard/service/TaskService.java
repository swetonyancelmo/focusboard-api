package com.swetonyancelmo.focusboard.service;

import com.swetonyancelmo.focusboard.dtos.request.CreateTaskRequestDTO;
import com.swetonyancelmo.focusboard.dtos.request.UpdateTaskRequestDTO;
import com.swetonyancelmo.focusboard.dtos.response.TaskResponseDTO;
import com.swetonyancelmo.focusboard.exceptions.ResourceNotFoundException;
import com.swetonyancelmo.focusboard.model.Task;
import com.swetonyancelmo.focusboard.model.User;
import com.swetonyancelmo.focusboard.model.enums.TaskPriority;
import com.swetonyancelmo.focusboard.model.enums.TaskStatus;
import com.swetonyancelmo.focusboard.repository.TaskRepository;
import com.swetonyancelmo.focusboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public List<TaskResponseDTO> findAllTasks(UUID userId) {
        return taskRepository.findByUserId(userId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional
    public TaskResponseDTO createTask(CreateTaskRequestDTO dto, UUID userId) {
        Task task = new Task();
        task.setTitle(dto.title());
        task.setDescription(dto.description());
        task.setStatus(dto.status() != null ? dto.status() : TaskStatus.TODO);
        task.setPriority(dto.priority() != null ? dto.priority() : TaskPriority.LOW);

        User userRef = userRepository.getReferenceById(userId);
        task.setUser(userRef);

        return toResponseDTO(taskRepository.save(task));
    }

    @Transactional
    public TaskResponseDTO updateTask(UUID taskId, UpdateTaskRequestDTO dto, UUID userId) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task não encontrada"));

        Optional.ofNullable(dto.title()).ifPresent(task::setTitle);
        Optional.ofNullable(dto.description()).ifPresent(task::setDescription);
        Optional.ofNullable(dto.status()).ifPresent(task::setStatus);
        Optional.ofNullable(dto.priority()).ifPresent(task::setPriority);

        return toResponseDTO(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(UUID taskId, UUID userId) {
        int deleted = taskRepository.deleteByIdAndUserId(taskId, userId);

        if (deleted == 0) {
            throw new ResourceNotFoundException("Task não encontrada");
        }
    }

    private TaskResponseDTO toResponseDTO(Task task) {
        return new TaskResponseDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus().toString(),
                task.getPriority().toString(),
                task.getUser().getId()
        );
    }
}
