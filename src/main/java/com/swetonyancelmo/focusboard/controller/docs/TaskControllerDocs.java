package com.swetonyancelmo.focusboard.controller.docs;

import com.swetonyancelmo.focusboard.dtos.request.CreateTaskRequestDTO;
import com.swetonyancelmo.focusboard.dtos.request.UpdateTaskRequestDTO;
import com.swetonyancelmo.focusboard.dtos.response.TaskResponseDTO;
import com.swetonyancelmo.focusboard.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

public interface TaskControllerDocs {
    @Operation(summary = "Listar Tarefas", description = "Listar todas as tarefas do usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tarefas listadas com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TaskResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Credenciais incorretas",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content)
    })
    ResponseEntity<List<TaskResponseDTO>> getAllTasks(@AuthenticationPrincipal User user);

    @Operation(summary = "Criar Tarefa", description = "Criar uma nova tarefa para o usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tarefa criada com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TaskResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Credenciais incorretas",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content)
    })
    ResponseEntity<TaskResponseDTO> createTask(@RequestBody @Valid CreateTaskRequestDTO dto, @AuthenticationPrincipal User user);

    @Operation(summary = "Atualizar Tarefa", description = "Atualizar uma tarefa existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tarefa atualizada com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TaskResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Credenciais incorretas",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content)
    })
    ResponseEntity<TaskResponseDTO> updateTask(
            @RequestBody @Valid UpdateTaskRequestDTO dto,
            @PathVariable UUID id,
            @AuthenticationPrincipal User user
    );

    @Operation(summary = "Excluir Tarefa", description = "Excluir uma tarefa existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Tarefa excluída com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TaskResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Credenciais incorretas",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content)
    })
    ResponseEntity<Void> deleteTask(@PathVariable UUID id, @AuthenticationPrincipal User user);
}
