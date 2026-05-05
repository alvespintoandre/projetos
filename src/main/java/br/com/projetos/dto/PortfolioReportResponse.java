package br.com.projetos.dto;

import br.com.projetos.domain.ProjectStatus;

import java.math.BigDecimal;
import java.util.Map;

public record PortfolioReportResponse(
        Map<ProjectStatus, Long> quantidadeProjetosPorStatus,
        Map<ProjectStatus, BigDecimal> totalOrcadoPorStatus,
        BigDecimal mediaDuracaoDiasProjetosEncerrados,
        long totalMembrosUnicosAlocados
) {
}
