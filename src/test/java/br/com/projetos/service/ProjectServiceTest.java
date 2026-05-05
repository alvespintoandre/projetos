package br.com.projetos.service;

import br.com.projetos.domain.Member;
import br.com.projetos.domain.MemberRole;
import br.com.projetos.domain.Project;
import br.com.projetos.domain.ProjectStatus;
import br.com.projetos.dto.ProjectCreateRequest;
import br.com.projetos.dto.ProjectUpdateRequest;
import br.com.projetos.dto.StatusTransitionRequest;
import br.com.projetos.exception.BusinessRuleException;
import br.com.projetos.exception.ResourceNotFoundException;
import br.com.projetos.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private MemberLookupService memberLookupService;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectService projectService;

    private Member gerente;
    private Member funcionario;

    @BeforeEach
    void setup() {
        gerente = new Member();
        gerente.setId(1L);
        gerente.setNome("Geraldo Alves");
        gerente.setAtribuicao(MemberRole.GERENTE);
        gerente.setCriadoEm(Instant.now());

        funcionario = new Member();
        funcionario.setId(2L);
        funcionario.setNome("Fernando Alves");
        funcionario.setAtribuicao(MemberRole.FUNCIONARIO);
        funcionario.setCriadoEm(Instant.now());
    }

    @Test
    void create_persiste_com_status_em_analise() {
        when(memberLookupService.require(1L)).thenReturn(gerente);
        when(memberLookupService.requireFuncionarios(Set.of(2L))).thenReturn(Set.of(funcionario));
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> {
            Project p = inv.getArgument(0);
            p.setId(99L);
            return p;
        });

        LocalDate inicio = LocalDate.now();
        LocalDate previsao = inicio.plusMonths(2);
        Project saved = new Project();
        saved.setId(99L);
        saved.setNome("Paulo Alves");
        saved.setDataInicio(inicio);
        saved.setPrevisaoTermino(previsao);
        saved.setOrcamentoTotal(BigDecimal.TEN);
        saved.setDescricao("descricao");
        saved.setGerente(gerente);
        saved.setStatus(ProjectStatus.EM_ANALISE);
        saved.setMembrosAlocados(new HashSet<>(Set.of(funcionario)));

        when(projectMapper.toResponse(any(Project.class))).thenReturn(
                new br.com.projetos.dto.ProjectResponse(
                        99L, "P", saved.getDataInicio(), saved.getPrevisaoTermino(), null,
                        BigDecimal.TEN, "d",
                        new br.com.projetos.dto.ProjectResponse.MemberSummary(1L, "G", "GERENTE"),
                        ProjectStatus.EM_ANALISE,
                        br.com.projetos.domain.RiskLevel.BAIXO,
                        Set.of(new br.com.projetos.dto.ProjectResponse.MemberSummary(2L, "F", "FUNCIONARIO"))
                ));

        projectService.create(new ProjectCreateRequest(
                "P",
                inicio,
                previsao,
                null,
                BigDecimal.TEN,
                "d",
                1L,
                Set.of(2L)));

        ArgumentCaptor<Project> captor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(ProjectStatus.EM_ANALISE);
    }

    @Test
    void delete_lanca_quando_status_bloqueia() {
        Project p = new Project();
        p.setId(5L);
        p.setNome("Mauro Silva");
        p.setDataInicio(LocalDate.now());
        p.setPrevisaoTermino(LocalDate.now().plusMonths(1));
        p.setOrcamentoTotal(BigDecimal.ONE);
        p.setDescricao("descricao");
        p.setGerente(gerente);
        p.setStatus(ProjectStatus.INICIADO);
        p.setMembrosAlocados(new HashSet<>(Set.of(funcionario)));

        when(projectRepository.findWithDetailsById(5L)).thenReturn(Optional.of(p));

        assertThatThrownBy(() -> projectService.delete(5L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("não pode ser excluído");
    }

    @Test
    void transicao_invalida_lanca() {
        Project p = new Project();
        p.setId(5L);
        p.setNome("Carlos Alves");
        p.setDataInicio(LocalDate.now());
        p.setPrevisaoTermino(LocalDate.now().plusMonths(1));
        p.setOrcamentoTotal(BigDecimal.ONE);
        p.setDescricao("descricao");
        p.setGerente(gerente);
        p.setStatus(ProjectStatus.EM_ANALISE);
        p.setMembrosAlocados(new HashSet<>(Set.of(funcionario)));

        when(projectRepository.findWithDetailsById(5L)).thenReturn(Optional.of(p));

        assertThatThrownBy(() -> projectService.transitionStatus(5L, new StatusTransitionRequest(ProjectStatus.INICIADO)))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void projeto_inexistente_lanca_not_found() {
        when(projectRepository.findWithDetailsById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.get(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_rejeita_previsao_anterior_ao_inicio() {
        LocalDate inicio = LocalDate.of(2025, 6, 1);
        LocalDate previsao = inicio.minusDays(1);

        assertThatThrownBy(() -> projectService.create(new ProjectCreateRequest(
                "Pedro Alves",
                inicio,
                previsao,
                null,
                BigDecimal.TEN,
                "d",
                1L,
                Set.of(2L))))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Previsão");
    }

    @Test
    void transicao_valida_atualiza_status() {
        Project p = new Project();
        p.setId(5L);
        p.setNome("Andre Pinto");
        LocalDate inicio = LocalDate.now();
        p.setDataInicio(inicio);
        p.setPrevisaoTermino(inicio.plusMonths(1));
        p.setOrcamentoTotal(BigDecimal.ONE);
        p.setDescricao("descricao");
        p.setGerente(gerente);
        p.setStatus(ProjectStatus.EM_ANALISE);
        p.setMembrosAlocados(new HashSet<>(Set.of(funcionario)));

        when(projectRepository.findWithDetailsById(5L)).thenReturn(Optional.of(p));
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> inv.getArgument(0));
        when(projectMapper.toResponse(any(Project.class))).thenAnswer(inv -> {
            Project arg = inv.getArgument(0);
            return new br.com.projetos.dto.ProjectResponse(
                    arg.getId(),
                    arg.getNome(),
                    arg.getDataInicio(),
                    arg.getPrevisaoTermino(),
                    arg.getDataRealTermino(),
                    arg.getOrcamentoTotal(),
                    arg.getDescricao(),
                    new br.com.projetos.dto.ProjectResponse.MemberSummary(1L, "G", "GERENTE"),
                    arg.getStatus(),
                    br.com.projetos.domain.RiskLevel.BAIXO,
                    Set.of());
        });

        projectService.transitionStatus(5L, new StatusTransitionRequest(ProjectStatus.ANALISE_REALIZADA));

        verify(projectRepository).save(argThat(proj -> proj.getStatus() == ProjectStatus.ANALISE_REALIZADA));
    }

    @Test
    void delete_remove_quando_status_permite() {
        Project p = new Project();
        p.setId(8L);
        p.setNome("Andre Alves Pinto");
        LocalDate inicio = LocalDate.now();
        p.setDataInicio(inicio);
        p.setPrevisaoTermino(inicio.plusMonths(1));
        p.setOrcamentoTotal(BigDecimal.ONE);
        p.setDescricao("descricao");
        p.setGerente(gerente);
        p.setStatus(ProjectStatus.EM_ANALISE);
        p.setMembrosAlocados(new HashSet<>(Set.of(funcionario)));

        when(projectRepository.findWithDetailsById(8L)).thenReturn(Optional.of(p));

        projectService.delete(8L);

        verify(projectRepository).delete(p);
    }

    @Test
    void update_persiste_alteracoes() {
        Project p = new Project();
        p.setId(3L);
        p.setNome("Andre Alves");
        LocalDate inicio = LocalDate.now();
        p.setDataInicio(inicio);
        p.setPrevisaoTermino(inicio.plusMonths(2));
        p.setOrcamentoTotal(BigDecimal.ONE);
        p.setDescricao("descricao");
        p.setGerente(gerente);
        p.setStatus(ProjectStatus.EM_ANALISE);
        p.setMembrosAlocados(new HashSet<>(Set.of(funcionario)));

        when(projectRepository.findWithDetailsById(3L)).thenReturn(Optional.of(p));
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> inv.getArgument(0));
        when(memberLookupService.require(1L)).thenReturn(gerente);
        when(projectMapper.toResponse(any(Project.class))).thenReturn(
                new br.com.projetos.dto.ProjectResponse(
                        3L,
                        "Andre Alves Pinto",
                        inicio,
                        inicio.plusMonths(3),
                        null,
                        BigDecimal.TEN,
                        "nova desc",
                        new br.com.projetos.dto.ProjectResponse.MemberSummary(1L, "G", "GERENTE"),
                        ProjectStatus.EM_ANALISE,
                        br.com.projetos.domain.RiskLevel.BAIXO,
                        Set.of()));

        projectService.update(3L, new ProjectUpdateRequest(
                "novo",
                inicio,
                inicio.plusMonths(3),
                null,
                BigDecimal.TEN,
                "nova desc",
                1L));

        verify(projectRepository).save(argThat(proj -> "novo".equals(proj.getNome()) && proj.getOrcamentoTotal().compareTo(BigDecimal.TEN) == 0));
    }
}
