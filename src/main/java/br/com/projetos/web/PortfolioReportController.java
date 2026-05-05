package br.com.projetos.web;

import br.com.projetos.dto.PortfolioReportResponse;
import br.com.projetos.service.PortfolioReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/relatorios")
@Tag(name = "Relatórios", description = "Resumo do portfólio")
public class PortfolioReportController {

    private final PortfolioReportService portfolioReportService;

    public PortfolioReportController(PortfolioReportService portfolioReportService) {
        this.portfolioReportService = portfolioReportService;
    }

    @GetMapping("/portfolio")
    @Operation(summary = "Relatório resumido do portfólio")
    public PortfolioReportResponse portfolio() {
        return portfolioReportService.generate();
    }
}
