package br.com.projetos.service;

import br.com.projetos.domain.Member;
import br.com.projetos.domain.MemberRole;
import br.com.projetos.domain.Project;
import br.com.projetos.domain.ProjectStatus;
import br.com.projetos.domain.RiskLevel;
import br.com.projetos.dto.ProjectResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectMapperTest {

    @Mock
    private RiskCalculationService riskCalculationService;

    @InjectMocks
    private ProjectMapper projectMapper;

    private Member gerente;
    private Member func;

    @BeforeEach
    void setUp() {
        gerente = new Member();
        gerente.setId(1L);
        gerente.setNome("G1");
        gerente.setAtribuicao(MemberRole.GERENTE);
        gerente.setCriadoEm(Instant.now());

        func = new Member();
        func.setId(2L);
        func.setNome("F1");
        func.setAtribuicao(MemberRole.FUNCIONARIO);
        func.setCriadoEm(Instant.now());
    }

    @Test
    void toResponse_mapeia_gerente_membros_e_risco() {
        when(riskCalculationService.classify(any(BigDecimal.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(RiskLevel.ALTO);

        LocalDate inicio = LocalDate.of(2025, 1, 1);
        LocalDate prev = inicio.plusMonths(4);
        Project p = new Project();
        p.setId(100L);
        p.setNome("Alpha");
        p.setDataInicio(inicio);
        p.setPrevisaoTermino(prev);
        p.setDataRealTermino(null);
        p.setOrcamentoTotal(new BigDecimal("600000"));
        p.setDescricao("desc");
        p.setGerente(gerente);
        p.setStatus(ProjectStatus.EM_ANDAMENTO);
        p.setMembrosAlocados(new HashSet<>(Set.of(func)));

        ProjectResponse r = projectMapper.toResponse(p);

        assertThat(r.id()).isEqualTo(100L);
        assertThat(r.nome()).isEqualTo("Alpha");
        assertThat(r.status()).isEqualTo(ProjectStatus.EM_ANDAMENTO);
        assertThat(r.classificacaoRisco()).isEqualTo(RiskLevel.ALTO);
        assertThat(r.gerente().nome()).isEqualTo("G1");
        assertThat(r.gerente().atribuicao()).isEqualTo("GERENTE");
        assertThat(r.membrosAlocados()).hasSize(1);
        assertThat(r.membrosAlocados().iterator().next().nome()).isEqualTo("F1");
    }
}
