package br.com.projetos.dto;

import br.com.projetos.domain.AppUserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AppUserCreateRequest(
        @NotBlank
        @Size(min = 2, max = 100)
        String username,

        @NotBlank
        @Size(min = 6, max = 128)
        String password,

        @NotNull
        AppUserRole role
) {
}
