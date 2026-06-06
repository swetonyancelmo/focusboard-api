package com.swetonyancelmo.focusboard.dtos.request;

import com.swetonyancelmo.focusboard.model.enums.TaskPriority;
import com.swetonyancelmo.focusboard.model.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTaskRequestDTO(
        @NotBlank(message = "O título é obrigatório")
        @Size(max = 255, message = "O título deve ter no máximo 255 caracteres")
        String title,

        @NotBlank(message = "A descrição é obrigatória")
        String description,

        TaskStatus status,
        TaskPriority priority
) {
}
