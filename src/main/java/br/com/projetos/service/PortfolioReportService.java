package br.com.projetos.service;

import br.com.projetos.domain.ProjectStatus;
import br.com.projetos.dto.PortfolioReportResponse;
import br.com.projetos.repository.PortfolioReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class PortfolioReportService {

    private final PortfolioReportRepository portfolioReportRepository;

    public PortfolioReportService(PortfolioReportRepository portfolioReportRepository) {
        this.portfolioReportRepository = portfolioReportRepository;
    }

    @Transactional(readOnly = true)
    public PortfolioReportResponse generate() {
        Map<ProjectStatus, Long> qtd = new EnumMap<>(ProjectStatus.class);
        Map<ProjectStatus, BigDecimal> total = new EnumMap<>(ProjectStatus.class);
        for (ProjectStatus s : ProjectStatus.values()) {
            qtd.put(s, 0L);
            total.put(s, BigDecimal.ZERO);
        }

        List<Object[]> rows = portfolioReportRepository.aggregateProjectsByStatus();
        for (Object[] row : rows) {
            String statusName = (String) row[0];
            ProjectStatus st = ProjectStatus.valueOf(statusName);
            long count = ((Number) row[1]).longValue();
            BigDecimal sum = row[2] instanceof BigDecimal b ? b : BigDecimal.valueOf(((Number) row[2]).doubleValue());
            qtd.put(st, count);
            total.put(st, sum);
        }

        BigDecimal mediaDuracao = portfolioReportRepository.averageDurationDaysEncerrados();
        long membrosUnicos = portfolioReportRepository.countDistinctAllocatedMembers();

        return new PortfolioReportResponse(qtd, total, mediaDuracao, membrosUnicos);
    }
}
