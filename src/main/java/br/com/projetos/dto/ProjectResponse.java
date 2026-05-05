package br.com.projetos.dto;

import br.com.projetos.domain.ProjectStatus;
import br.com.projetos.domain.RiskLevel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public record ProjectResponse(
        Long id,
        String nome,
        LocalDate dataInicio,
        LocalDate previsaoTermino,
        LocalDate dataRealTermino,
        BigDecimal orcamentoTotal,
        String descricao,
        MemberSummary gerente,
        ProjectStatus status,
        RiskLevel classificacaoRisco,
        Set<MemberSummary> membrosAlocados
) {
    public record MemberSummary(Long id, String nome, String atribuicao) {
    }
}
