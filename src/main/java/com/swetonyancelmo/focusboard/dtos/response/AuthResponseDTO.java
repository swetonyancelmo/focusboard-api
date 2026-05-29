package com.swetonyancelmo.focusboard.dtos.response;

public record AuthResponseDTO(
        String accessToken,
        String refreshToken
) {
    public static AuthResponseDTO of(String accessToken, String refreshToken) {
         return new AuthResponseDTO(accessToken, refreshToken);
    }
}
