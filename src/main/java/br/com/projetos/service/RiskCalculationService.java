package br.com.projetos.service;

import br.com.projetos.domain.RiskLevel;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class RiskCalculationService {

    static final BigDecimal LIMITE_BAIXO_ORCAMENTO = new BigDecimal("100000");
    static final BigDecimal LIMITE_ALTO_ORCAMENTO = new BigDecimal("500000");

    public RiskLevel classify(BigDecimal orcamentoTotal, LocalDate dataInicio, LocalDate previsaoTermino) {
        long meses = Math.max(0, ChronoUnit.MONTHS.between(dataInicio, previsaoTermino));

        boolean altoOrcamento = orcamentoTotal.compareTo(LIMITE_ALTO_ORCAMENTO) > 0;
        boolean altoPrazo = meses > 6;
        if (altoOrcamento || altoPrazo) {
            return RiskLevel.ALTO;
        }

        boolean medioOrcamento = orcamentoTotal.compareTo(LIMITE_BAIXO_ORCAMENTO) > 0
                && orcamentoTotal.compareTo(LIMITE_ALTO_ORCAMENTO) <= 0;
        boolean medioPrazo = meses > 3 && meses <= 6;
        if (medioOrcamento || medioPrazo) {
            return RiskLevel.MEDIO;
        }

        boolean baixo = orcamentoTotal.compareTo(LIMITE_BAIXO_ORCAMENTO) <= 0 && meses <= 3;
        if (baixo) {
            return RiskLevel.BAIXO;
        }

        return RiskLevel.MEDIO;
    }
}
