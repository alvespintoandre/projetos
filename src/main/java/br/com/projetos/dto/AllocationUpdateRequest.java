package br.com.projetos.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record AllocationUpdateRequest(
        @NotNull
        @NotEmpty
        @Size(max = 10)
        Set<Long> membrosIds
) {
}
