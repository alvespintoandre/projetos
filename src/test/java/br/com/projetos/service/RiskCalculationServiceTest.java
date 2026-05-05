package br.com.projetos.service;

import br.com.projetos.domain.RiskLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class RiskCalculationServiceTest {

    private RiskCalculationService service;

    @BeforeEach
    void setUp() {
        service = new RiskCalculationService();
    }

    @Test
    @DisplayName("Baixo: orçamento até 100k e prazo até 3 meses")
    void baixo_risco() {
        LocalDate inicio = LocalDate.of(2025, 1, 1);
        LocalDate fim = inicio.plusMonths(3);
        RiskLevel r = service.classify(new BigDecimal("100000"), inicio, fim);
        assertThat(r).isEqualTo(RiskLevel.BAIXO);
    }

    @Test
    @DisplayName("Alto: orçamento acima de 500k")
    void alto_por_orcamento() {
        LocalDate inicio = LocalDate.of(2025, 1, 1);
        LocalDate fim = inicio.plusMonths(1);
        RiskLevel r = service.classify(new BigDecimal("500000.01"), inicio, fim);
        assertThat(r).isEqualTo(RiskLevel.ALTO);
    }

    @Test
    @DisplayName("Alto: prazo superior a 6 meses")
    void alto_por_prazo() {
        LocalDate inicio = LocalDate.of(2025, 1, 1);
        LocalDate fim = inicio.plusMonths(7);
        RiskLevel r = service.classify(new BigDecimal("50000"), inicio, fim);
        assertThat(r).isEqualTo(RiskLevel.ALTO);
    }

    @Test
    @DisplayName("Médio: orçamento entre 100.001 e 500.000")
    void medio_por_faixa_orcamento() {
        LocalDate inicio = LocalDate.of(2025, 1, 1);
        LocalDate fim = inicio.plusMonths(2);
        RiskLevel r = service.classify(new BigDecimal("200000"), inicio, fim);
        assertThat(r).isEqualTo(RiskLevel.MEDIO);
    }

    @Test
    @DisplayName("Médio: prazo entre 3 e 6 meses")
    void medio_por_faixa_prazo() {
        LocalDate inicio = LocalDate.of(2025, 1, 1);
        LocalDate fim = inicio.plusMonths(5);
        RiskLevel r = service.classify(new BigDecimal("50000"), inicio, fim);
        assertThat(r).isEqualTo(RiskLevel.MEDIO);
    }

    @Test
    @DisplayName("Orçamento exatamente 500k com prazo curto é médio (não alto)")
    void limite_500k_medio() {
        LocalDate inicio = LocalDate.of(2025, 1, 1);
        LocalDate fim = inicio.plusMonths(2);
        RiskLevel r = service.classify(new BigDecimal("500000"), inicio, fim);
        assertThat(r).isEqualTo(RiskLevel.MEDIO);
    }
}
