package br.com.projetos.service;

import br.com.projetos.domain.Member;
import br.com.projetos.domain.MemberRole;
import br.com.projetos.exception.BusinessRuleException;
import br.com.projetos.exception.ResourceNotFoundException;
import br.com.projetos.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberLookupServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberLookupService memberLookupService;

    private Member funcionario;
    private Member gerente;

    @BeforeEach
    void setup() {
        funcionario = new Member();
        funcionario.setId(10L);
        funcionario.setNome("Fernando");
        funcionario.setAtribuicao(MemberRole.FUNCIONARIO);
        funcionario.setCriadoEm(Instant.now());

        gerente = new Member();
        gerente.setId(11L);
        gerente.setNome("Geraldo");
        gerente.setAtribuicao(MemberRole.GERENTE);
        gerente.setCriadoEm(Instant.now());
    }

    @Test
    void require_funcionario_ok() {
        when(memberRepository.findById(10L)).thenReturn(Optional.of(funcionario));
        Set<Member> r = memberLookupService.requireFuncionarios(List.of(10L));
        assertThat(r).containsExactly(funcionario);
    }

    @Test
    void rejeita_nao_funcionario() {
        when(memberRepository.findById(11L)).thenReturn(Optional.of(gerente));

        assertThatThrownBy(() -> memberLookupService.requireFuncionarios(List.of(11L)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("funcionário");
    }

    @Test
    void valida_cap_quarto_projeto() {
        when(memberRepository.countActiveProjectsForMember(
                10L,
                br.com.projetos.domain.ProjectStatus.ENCERRADO,
                br.com.projetos.domain.ProjectStatus.CANCELADO)).thenReturn(3L);

        assertThatThrownBy(() -> memberLookupService.validateProjectCapForMembers(Set.of(funcionario), null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("3 projetos");
    }

    @Test
    void require_lanca_quando_membro_inexistente() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberLookupService.require(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void require_funcionarios_lista_vazia_lanca() {
        assertThatThrownBy(() -> memberLookupService.requireFuncionarios(List.of()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("alocar");
    }

    @Test
    void valida_cap_usa_query_com_exclusao_de_projeto() {
        when(memberRepository.countActiveProjectsForMemberExcluding(
                10L,
                7L,
                br.com.projetos.domain.ProjectStatus.ENCERRADO,
                br.com.projetos.domain.ProjectStatus.CANCELADO)).thenReturn(3L);

        assertThatThrownBy(() -> memberLookupService.validateProjectCapForMembers(Set.of(funcionario), 7L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("3 projetos");
    }
}
