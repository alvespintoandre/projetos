package br.com.projetos.service;

import br.com.projetos.domain.Member;
import br.com.projetos.domain.MemberRole;
import br.com.projetos.dto.MemberCreateRequest;
import br.com.projetos.dto.MemberResponse;
import br.com.projetos.exception.ResourceNotFoundException;
import br.com.projetos.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberExternalServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberExternalService memberExternalService;

    @Test
    void create_persiste_e_retorna_dto() {
        when(memberRepository.save(any(Member.class))).thenAnswer(inv -> {
            Member m = inv.getArgument(0);
            m.setId(42L);
            return m;
        });

        MemberResponse r = memberExternalService.create(new MemberCreateRequest("Ana", MemberRole.FUNCIONARIO));

        assertThat(r.id()).isEqualTo(42L);
        assertThat(r.nome()).isEqualTo("Ana");
        assertThat(r.atribuicao()).isEqualTo(MemberRole.FUNCIONARIO);
        assertThat(r.criadoEm()).isNotNull();

        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(captor.capture());
        assertThat(captor.getValue().getNome()).isEqualTo("Ana");
        assertThat(captor.getValue().getAtribuicao()).isEqualTo(MemberRole.FUNCIONARIO);
    }

    @Test
    void get_retorna_quando_existe() {
        Member m = new Member();
        m.setId(1L);
        m.setNome("Beto");
        m.setAtribuicao(MemberRole.GERENTE);
        m.setCriadoEm(Instant.parse("2025-06-01T12:00:00Z"));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(m));

        MemberResponse r = memberExternalService.get(1L);

        assertThat(r.id()).isEqualTo(1L);
        assertThat(r.nome()).isEqualTo("Beto");
        assertThat(r.atribuicao()).isEqualTo(MemberRole.GERENTE);
    }

    @Test
    void get_lanca_quando_inexistente() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberExternalService.get(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void listAll_retorna_todos_mapeados() {
        Member a = new Member();
        a.setId(1L);
        a.setNome("Andre");
        a.setAtribuicao(MemberRole.FUNCIONARIO);
        a.setCriadoEm(Instant.now());
        when(memberRepository.findAll()).thenReturn(List.of(a));

        List<MemberResponse> list = memberExternalService.listAll();

        assertThat(list).hasSize(1);
        assertThat(list.getFirst().nome()).isEqualTo("Andre");
    }
}
