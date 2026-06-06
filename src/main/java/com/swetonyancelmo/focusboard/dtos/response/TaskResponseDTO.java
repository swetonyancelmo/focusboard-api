package com.swetonyancelmo.focusboard.dtos.response;

import java.util.UUID;

public record TaskResponseDTO(
        UUID id,
        String title,
        String description,
        String status,
        String priority,
        UUID userId
) {
}
