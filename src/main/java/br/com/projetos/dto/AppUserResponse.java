package br.com.projetos.dto;

import br.com.projetos.domain.AppUserRole;

public record AppUserResponse(Long id, String username, AppUserRole role) {
}
