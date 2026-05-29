package com.swetonyancelmo.focusboard.controller.docs;

import com.swetonyancelmo.focusboard.dtos.request.LoginRequestDTO;
import com.swetonyancelmo.focusboard.dtos.request.RefreshTokenRequestDTO;
import com.swetonyancelmo.focusboard.dtos.request.RegisterRequestDTO;
import com.swetonyancelmo.focusboard.dtos.response.AuthResponseDTO;
import com.swetonyancelmo.focusboard.dtos.response.RegisterResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface AuthControllerDocs {
    @Operation(summary = "Register", description = "Realizar o cadastro de um novo usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RegisterResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de cadastro inválidos",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Credenciais incorretas",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content)
    })
    ResponseEntity<RegisterResponseDTO> register(@Valid @RequestBody RegisterRequestDTO dto);

    @Operation(summary = "Login", description = "Autentica um usuário e retorna um token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de login inválidos",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Credenciais incorretas",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content)
    })
    ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto);

    @Operation(summary = "Refresh", description = "Renova o token JWT usando um refresh token válido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token renovado com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de refresh inválidos",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Credenciais incorretas",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content)
    })
    ResponseEntity<AuthResponseDTO> refresh(@Valid @RequestBody RefreshTokenRequestDTO dto);

    @Operation(summary = "Logout", description = "Realiza o logout do usuário, invalidando o token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Logout realizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Credenciais incorretas",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content)
    })
    ResponseEntity<Void> logout(Authentication authentication);
}
