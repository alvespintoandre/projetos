package br.com.projetos.web;

import br.com.projetos.dto.AppUserCreateRequest;
import br.com.projetos.dto.AppUserResponse;
import br.com.projetos.service.AppUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/usuarios")
@Tag(name = "Admin — Usuários da API", description = "Cadastro e listagem (apenas ADMIN)")
@SecurityRequirement(name = "basicAuth")
public class AdminUserController {

    private final AppUserService appUserService;

    public AdminUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @GetMapping
    @Operation(summary = "Listar todos os usuários de login da aplicação")
    public List<AppUserResponse> list() {
        return appUserService.listAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por id")
    public AppUserResponse get(@PathVariable Long id) {
        return appUserService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar usuário (senha armazenada com hash)")
    public AppUserResponse create(@Valid @RequestBody AppUserCreateRequest request) {
        return appUserService.create(request);
    }
}
