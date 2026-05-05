package br.com.projetos.dto;

import br.com.projetos.domain.ProjectStatus;
import jakarta.validation.constraints.NotNull;

public record StatusTransitionRequest(
        @NotNull
        ProjectStatus novoStatus
) {
}
