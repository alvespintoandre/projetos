package br.com.projetos.service;

import br.com.projetos.domain.Member;
import br.com.projetos.domain.Project;
import br.com.projetos.domain.ProjectStatus;
import br.com.projetos.dto.AllocationUpdateRequest;
import br.com.projetos.dto.ProjectCreateRequest;
import br.com.projetos.dto.ProjectResponse;
import br.com.projetos.dto.ProjectUpdateRequest;
import br.com.projetos.dto.StatusTransitionRequest;
import br.com.projetos.exception.BusinessRuleException;
import br.com.projetos.exception.ResourceNotFoundException;
import br.com.projetos.repository.ProjectRepository;
import br.com.projetos.repository.ProjectSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final MemberLookupService memberLookupService;
    private final ProjectMapper projectMapper;

    public ProjectService(
            ProjectRepository projectRepository,
            MemberLookupService memberLookupService,
            ProjectMapper projectMapper) {
        this.projectRepository = projectRepository;
        this.memberLookupService = memberLookupService;
        this.projectMapper = projectMapper;
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponse> list(String nomeFiltro, ProjectStatus statusFiltro, Pageable pageable) {
        Specification<Project> spec = ProjectSpecifications.combine(nomeFiltro, statusFiltro);
        return projectRepository.findAll(spec, pageable).map(projectMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ProjectResponse get(Long id) {
        return projectMapper.toResponse(requireProject(id));
    }

    @Transactional
    public ProjectResponse create(ProjectCreateRequest request) {
        validateDates(request.dataInicio(), request.previsaoTermino(), request.dataRealTermino());

        Member gerente = memberLookupService.require(request.gerenteId());
        Set<Member> alocados = memberLookupService.requireFuncionarios(request.membrosAlocadosIds());
        memberLookupService.validateProjectCapForMembers(alocados, null);

        Project project = new Project();
        project.setNome(request.nome());
        project.setDataInicio(request.dataInicio());
        project.setPrevisaoTermino(request.previsaoTermino());
        project.setDataRealTermino(request.dataRealTermino());
        project.setOrcamentoTotal(request.orcamentoTotal());
        project.setDescricao(request.descricao());
        project.setGerente(gerente);
        project.setStatus(ProjectStatus.EM_ANALISE);
        project.setMembrosAlocados(new HashSet<>(alocados));

        Project saved = projectRepository.save(project);
        return projectMapper.toResponse(saved);
    }

    @Transactional
    public ProjectResponse update(Long id, ProjectUpdateRequest request) {
        Project project = requireProject(id);
        validateDates(request.dataInicio(), request.previsaoTermino(), request.dataRealTermino());

        Member gerente = memberLookupService.require(request.gerenteId());
        project.setNome(request.nome());
        project.setDataInicio(request.dataInicio());
        project.setPrevisaoTermino(request.previsaoTermino());
        project.setDataRealTermino(request.dataRealTermino());
        project.setOrcamentoTotal(request.orcamentoTotal());
        project.setDescricao(request.descricao());
        project.setGerente(gerente);

        return projectMapper.toResponse(projectRepository.save(project));
    }

    @Transactional
    public ProjectResponse updateAllocations(Long id, AllocationUpdateRequest request) {
        Project project = requireProject(id);
        Set<Member> alocados = memberLookupService.requireFuncionarios(request.membrosIds());
        memberLookupService.validateProjectCapForMembers(alocados, id);
        project.getMembrosAlocados().clear();
        project.getMembrosAlocados().addAll(alocados);
        return projectMapper.toResponse(projectRepository.save(project));
    }

    @Transactional
    public ProjectResponse transitionStatus(Long id, StatusTransitionRequest request) {
        Project project = requireProject(id);
        ProjectStatus atual = project.getStatus();
        ProjectStatus novo = request.novoStatus();

        if (!atual.canTransitionTo(novo)) {
            throw new BusinessRuleException(
                    "Transição inválida de " + atual + " para " + novo + ". Respeite a sequência ou use cancelado.");
        }

        project.setStatus(novo);
        if (novo == ProjectStatus.ENCERRADO && project.getDataRealTermino() == null) {
            project.setDataRealTermino(LocalDate.now());
        }

        return projectMapper.toResponse(projectRepository.save(project));
    }

    @Transactional
    public void delete(Long id) {
        Project project = requireProject(id);
        if (project.getStatus().blocksDeletion()) {
            throw new BusinessRuleException(
                    "Projeto não pode ser excluído com status " + project.getStatus());
        }
        projectRepository.delete(project);
    }

    private Project requireProject(Long id) {
        return projectRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado: " + id));
    }

    private void validateDates(LocalDate inicio, LocalDate previsao, LocalDate real) {
        if (previsao.isBefore(inicio)) {
            throw new BusinessRuleException("Previsão de término não pode ser anterior à data de início.");
        }
        if (real != null && real.isBefore(inicio)) {
            throw new BusinessRuleException("Data real de término não pode ser anterior à data de início.");
        }
    }
}
