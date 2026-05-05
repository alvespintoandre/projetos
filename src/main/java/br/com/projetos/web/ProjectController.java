package br.com.projetos.web;

import br.com.projetos.domain.ProjectStatus;
import br.com.projetos.dto.AllocationUpdateRequest;
import br.com.projetos.dto.ProjectCreateRequest;
import br.com.projetos.dto.ProjectResponse;
import br.com.projetos.dto.ProjectUpdateRequest;
import br.com.projetos.dto.StatusTransitionRequest;
import br.com.projetos.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projetos")
@Tag(name = "Projetos", description = "CRUD e operações de portfólio")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    @Operation(summary = "Listagem paginada com filtros opcionais por nome e status")
    public Page<ProjectResponse> list(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) ProjectStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return projectService.list(nome, status, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar projeto por id")
    public ProjectResponse get(@PathVariable Long id) {
        return projectService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar projeto (status inicial: em análise)")
    public ProjectResponse create(@Valid @RequestBody ProjectCreateRequest request) {
        return projectService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar dados cadastrais do projeto")
    public ProjectResponse update(@PathVariable Long id, @Valid @RequestBody ProjectUpdateRequest request) {
        return projectService.update(id, request);
    }

    @PutMapping("/{id}/alocacoes")
    @Operation(summary = "Substituir membros alocados (1 a 10 funcionários)")
    public ProjectResponse updateAllocations(@PathVariable Long id, @Valid @RequestBody AllocationUpdateRequest request) {
        return projectService.updateAllocations(id, request);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Transição de status respeitando a sequência ou cancelamento")
    public ProjectResponse transition(@PathVariable Long id, @Valid @RequestBody StatusTransitionRequest request) {
        return projectService.transitionStatus(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir projeto (bloqueado para iniciado, em andamento e encerrado)")
    public void delete(@PathVariable Long id) {
        projectService.delete(id);
    }
}
