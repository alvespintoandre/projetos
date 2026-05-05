package br.com.projetos.web;

import br.com.projetos.dto.MemberCreateRequest;
import br.com.projetos.dto.MemberResponse;
import br.com.projetos.service.MemberExternalService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/externo/membros")
@Tag(name = "Membros (externo)", description = "API mockada — apenas ADMIN")
public class ExternalMemberController {

    private final MemberExternalService memberExternalService;

    public ExternalMemberController(MemberExternalService memberExternalService) {
        this.memberExternalService = memberExternalService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar membro com nome e atribuição")
    public MemberResponse create(@Valid @RequestBody MemberCreateRequest request) {
        return memberExternalService.create(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar membro por id")
    public MemberResponse get(@PathVariable Long id) {
        return memberExternalService.get(id);
    }

    @GetMapping
    @Operation(summary = "Listar todos os membros")
    public List<MemberResponse> list() {
        return memberExternalService.listAll();
    }
}
