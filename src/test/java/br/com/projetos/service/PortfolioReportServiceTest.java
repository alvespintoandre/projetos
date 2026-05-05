package br.com.projetos.service;

import br.com.projetos.domain.ProjectStatus;
import br.com.projetos.dto.PortfolioReportResponse;
import br.com.projetos.repository.PortfolioReportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortfolioReportServiceTest {

    @Mock
    private PortfolioReportRepository portfolioReportRepository;

    @InjectMocks
    private PortfolioReportService portfolioReportService;

    @Test
    void generate_agrega_linhas_e_metricas() {
        when(portfolioReportRepository.aggregateProjectsByStatus()).thenReturn(List.of(
                new Object[]{"EM_ANALISE", 2L, new BigDecimal("1500.50")},
                new Object[]{"ENCERRADO", 1L, new BigDecimal("99.00")}
        ));
        when(portfolioReportRepository.averageDurationDaysEncerrados()).thenReturn(new BigDecimal("12.5"));
        when(portfolioReportRepository.countDistinctAllocatedMembers()).thenReturn(7L);

        PortfolioReportResponse r = portfolioReportService.generate();

        assertThat(r.quantidadeProjetosPorStatus().get(ProjectStatus.EM_ANALISE)).isEqualTo(2L);
        assertThat(r.quantidadeProjetosPorStatus().get(ProjectStatus.ENCERRADO)).isEqualTo(1L);
        assertThat(r.totalOrcadoPorStatus().get(ProjectStatus.EM_ANALISE)).isEqualByComparingTo("1500.50");
        assertThat(r.mediaDuracaoDiasProjetosEncerrados()).isEqualByComparingTo("12.5");
        assertThat(r.totalMembrosUnicosAlocados()).isEqualTo(7L);
        assertThat(r.quantidadeProjetosPorStatus().get(ProjectStatus.CANCELADO)).isZero();
    }

    @Test
    void generate_sem_linhas_mantem_zeros() {
        when(portfolioReportRepository.aggregateProjectsByStatus()).thenReturn(List.of());
        when(portfolioReportRepository.averageDurationDaysEncerrados()).thenReturn(BigDecimal.ZERO);
        when(portfolioReportRepository.countDistinctAllocatedMembers()).thenReturn(0L);

        PortfolioReportResponse r = portfolioReportService.generate();

        assertThat(r.quantidadeProjetosPorStatus().values()).allMatch(v -> v == 0L);
        assertThat(r.totalOrcadoPorStatus().values()).allMatch(v -> v.compareTo(BigDecimal.ZERO) == 0);
        assertThat(r.totalMembrosUnicosAlocados()).isZero();
    }
}
