package br.com.projetos.domain;

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum ProjectStatus {
    EM_ANALISE,
    ANALISE_REALIZADA,
    ANALISE_APROVADA,
    INICIADO,
    PLANEJADO,
    EM_ANDAMENTO,
    ENCERRADO,
    CANCELADO;

    private static final Map<ProjectStatus, ProjectStatus> NEXT_IN_SEQUENCE = Map.of(
            EM_ANALISE, ANALISE_REALIZADA,
            ANALISE_REALIZADA, ANALISE_APROVADA,
            ANALISE_APROVADA, INICIADO,
            INICIADO, PLANEJADO,
            PLANEJADO, EM_ANDAMENTO,
            EM_ANDAMENTO, ENCERRADO
    );

    private static final Set<ProjectStatus> BLOCKS_DELETE = EnumSet.of(INICIADO, EM_ANDAMENTO, ENCERRADO);

    private static final Map<String, ProjectStatus> BY_NAME = java.util.Arrays.stream(values())
            .collect(Collectors.toMap(Enum::name, Function.identity()));

    public static Optional<ProjectStatus> fromString(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(BY_NAME.get(raw.trim()));
    }

    public boolean blocksDeletion() {
        return BLOCKS_DELETE.contains(this);
    }

    public boolean isTerminal() {
        return this == ENCERRADO || this == CANCELADO;
    }

    public boolean isCancelamento() {
        return this == CANCELADO;
    }

    public Optional<ProjectStatus> nextInSequence() {
        return Optional.ofNullable(NEXT_IN_SEQUENCE.get(this));
    }

    public boolean canTransitionTo(ProjectStatus target) {
        if (target == CANCELADO) {
            return true;
        }
        return NEXT_IN_SEQUENCE.get(this) == target;
    }
}
