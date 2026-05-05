package br.com.projetos.service;

import br.com.projetos.domain.Member;
import br.com.projetos.domain.Project;
import br.com.projetos.dto.ProjectResponse;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ProjectMapper {

    private final RiskCalculationService riskCalculationService;

    public ProjectMapper(RiskCalculationService riskCalculationService) {
        this.riskCalculationService = riskCalculationService;
    }

    public ProjectResponse toResponse(Project project) {
        var risco = riskCalculationService.classify(
                project.getOrcamentoTotal(),
                project.getDataInicio(),
                project.getPrevisaoTermino());

        Member g = project.getGerente();
        var gerenteSummary = new ProjectResponse.MemberSummary(
                g.getId(),
                g.getNome(),
                g.getAtribuicao().name());

        Set<ProjectResponse.MemberSummary> alocados = project.getMembrosAlocados().stream()
                .map(m -> new ProjectResponse.MemberSummary(m.getId(), m.getNome(), m.getAtribuicao().name()))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return new ProjectResponse(
                project.getId(),
                project.getNome(),
                project.getDataInicio(),
                project.getPrevisaoTermino(),
                project.getDataRealTermino(),
                project.getOrcamentoTotal(),
                project.getDescricao(),
                gerenteSummary,
                project.getStatus(),
                risco,
                alocados
        );
    }
}
