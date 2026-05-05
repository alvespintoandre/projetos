package br.com.projetos.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectStatusTest {

    @Test
    void sequencia_em_analise_para_analise_realizada() {
        assertThat(ProjectStatus.EM_ANALISE.canTransitionTo(ProjectStatus.ANALISE_REALIZADA)).isTrue();
        assertThat(ProjectStatus.EM_ANALISE.canTransitionTo(ProjectStatus.INICIADO)).isFalse();
    }

    @Test
    void cancelado_em_qualquer_momento() {
        assertThat(ProjectStatus.PLANEJADO.canTransitionTo(ProjectStatus.CANCELADO)).isTrue();
        assertThat(ProjectStatus.EM_ANALISE.canTransitionTo(ProjectStatus.CANCELADO)).isTrue();
    }

    @Test
    void encerrado_sem_proximo_passo() {
        assertThat(ProjectStatus.EM_ANDAMENTO.canTransitionTo(ProjectStatus.ENCERRADO)).isTrue();
        assertThat(ProjectStatus.ENCERRADO.nextInSequence()).isEmpty();
    }

    @Test
    void exclusao_bloqueada_para_alguns_status() {
        assertThat(ProjectStatus.INICIADO.blocksDeletion()).isTrue();
        assertThat(ProjectStatus.EM_ANDAMENTO.blocksDeletion()).isTrue();
        assertThat(ProjectStatus.ENCERRADO.blocksDeletion()).isTrue();
        assertThat(ProjectStatus.EM_ANALISE.blocksDeletion()).isFalse();
    }

    @Test
    void fromString_resolve_nome_do_enum() {
        assertThat(ProjectStatus.fromString(" EM_ANALISE ").orElseThrow()).isEqualTo(ProjectStatus.EM_ANALISE);
        assertThat(ProjectStatus.fromString(null)).isEmpty();
        assertThat(ProjectStatus.fromString("")).isEmpty();
    }

    @Test
    void status_terminal_e_cancelamento() {
        assertThat(ProjectStatus.ENCERRADO.isTerminal()).isTrue();
        assertThat(ProjectStatus.CANCELADO.isTerminal()).isTrue();
        assertThat(ProjectStatus.EM_ANALISE.isTerminal()).isFalse();

        assertThat(ProjectStatus.CANCELADO.isCancelamento()).isTrue();
        assertThat(ProjectStatus.INICIADO.isCancelamento()).isFalse();
    }
}
