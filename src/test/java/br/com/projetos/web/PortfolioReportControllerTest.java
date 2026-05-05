package br.com.projetos.web;

import br.com.projetos.domain.ProjectStatus;
import br.com.projetos.dto.PortfolioReportResponse;
import br.com.projetos.service.PortfolioReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PortfolioReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class PortfolioReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PortfolioReportService portfolioReportService;

    @Test
    void portfolio_retorna_relatorio() throws Exception {
        Map<ProjectStatus, Long> qtd = new EnumMap<>(ProjectStatus.class);
        qtd.put(ProjectStatus.EM_ANALISE, 1L);
        Map<ProjectStatus, BigDecimal> tot = new EnumMap<>(ProjectStatus.class);
        tot.put(ProjectStatus.EM_ANALISE, BigDecimal.TEN);

        when(portfolioReportService.generate()).thenReturn(new PortfolioReportResponse(qtd, tot, BigDecimal.ONE, 2L));

        mockMvc.perform(get("/api/relatorios/portfolio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMembrosUnicosAlocados").value(2))
                .andExpect(jsonPath("$.quantidadeProjetosPorStatus.EM_ANALISE").value(1));

        verify(portfolioReportService).generate();
    }
}
