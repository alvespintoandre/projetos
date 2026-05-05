package br.com.projetos.dto;

import br.com.projetos.domain.MemberRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MemberCreateRequest(
        @NotBlank
        @Size(max = 200)
        String nome,

        @NotNull
        MemberRole atribuicao
) {
}
