package com.swetonyancelmo.focusboard.dtos.response;

import java.util.UUID;

public record RegisterResponseDTO (
        UUID id,
        String name,
        String email
) {
}
