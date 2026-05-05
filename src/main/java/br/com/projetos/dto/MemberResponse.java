package br.com.projetos.dto;

import br.com.projetos.domain.MemberRole;

import java.time.Instant;

public record MemberResponse(
        Long id,
        String nome,
        MemberRole atribuicao,
        Instant criadoEm
) {
}
