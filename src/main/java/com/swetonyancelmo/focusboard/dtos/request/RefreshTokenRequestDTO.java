package com.swetonyancelmo.focusboard.dtos.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequestDTO(
        @NotBlank(message = "O refreshToken é obrigatório")
        String refreshToken
) {
}
