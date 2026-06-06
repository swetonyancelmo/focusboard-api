package com.swetonyancelmo.focusboard.controller;

import com.swetonyancelmo.focusboard.controller.docs.TaskControllerDocs;
import com.swetonyancelmo.focusboard.dtos.request.CreateTaskRequestDTO;
import com.swetonyancelmo.focusboard.dtos.request.UpdateTaskRequestDTO;
import com.swetonyancelmo.focusboard.dtos.response.TaskResponseDTO;
import com.swetonyancelmo.focusboard.model.User;
import com.swetonyancelmo.focusboard.service.TaskService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Task Endpoints", description = "Endpoints para gerenciamento de tarefas")
@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController implements TaskControllerDocs {

    private final TaskService taskService;

    @GetMapping
    @Override
    public ResponseEntity<Page<TaskResponseDTO>> getAllTasks(
            @AuthenticationPrincipal User user,
            @RequestParam(value = "page", defaultValue = "0") @Min(0) Integer page,
            @RequestParam(value = "size", defaultValue = "12") @Positive Integer size,
            @RequestParam(value = "direction", defaultValue = "asc") String direction
    ) {
        var sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, "title"));
        return ResponseEntity.ok(taskService.findAllTasks(user.getId(), pageable));
    }

    @PostMapping
    @Override
    public ResponseEntity<TaskResponseDTO> createTask(@RequestBody @Valid CreateTaskRequestDTO dto, @AuthenticationPrincipal User user) {
        TaskResponseDTO response = taskService.createTask(dto, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    @Override
    public ResponseEntity<TaskResponseDTO> updateTask(
            @RequestBody @Valid UpdateTaskRequestDTO dto,
            @PathVariable UUID id,
            @AuthenticationPrincipal User user
    ) {
        TaskResponseDTO response = taskService.updateTask(id, dto, user.getId());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        taskService.deleteTask(id, user.getId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
