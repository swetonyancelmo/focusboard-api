package com.swetonyancelmo.focusboard.dtos.request;

import com.swetonyancelmo.focusboard.model.enums.TaskPriority;
import com.swetonyancelmo.focusboard.model.enums.TaskStatus;
import jakarta.validation.constraints.Size;

public record UpdateTaskRequestDTO(
        @Size(min = 3, max = 255, message = "O título deve ter entre 3 e 255 caracteres")
        String title,

        String description,
        TaskStatus status,
        TaskPriority priority
) {
}
