package br.com.projetos.service;

import br.com.projetos.domain.AppUser;
import br.com.projetos.domain.AppUserRole;
import br.com.projetos.dto.AppUserCreateRequest;
import br.com.projetos.exception.BusinessRuleException;
import br.com.projetos.exception.ResourceNotFoundException;
import br.com.projetos.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppUserServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AppUserService appUserService;

    @Test
    void create_codifica_senha_e_salva() {
        when(appUserRepository.existsByUsername("joao")).thenReturn(false);
        when(passwordEncoder.encode("secret12")).thenReturn("{bcrypt}hash");
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(inv -> {
            AppUser u = inv.getArgument(0);
            u.setId(5L);
            return u;
        });

        var res = appUserService.create(new AppUserCreateRequest("joao", "secret12", AppUserRole.USER));

        assertThat(res.id()).isEqualTo(5L);
        assertThat(res.username()).isEqualTo("joao");
        assertThat(res.role()).isEqualTo(AppUserRole.USER);

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("{bcrypt}hash");
    }

    @Test
    void create_rejeita_login_duplicado() {
        when(appUserRepository.existsByUsername("joao")).thenReturn(true);

        assertThatThrownBy(() -> appUserService.create(new AppUserCreateRequest("joao", "secret12", AppUserRole.USER)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Já existe");
    }

    @Test
    void getById_lanca_quando_inexistente() {
        when(appUserRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appUserService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listAll_ordenado_por_username() {
        AppUser a = new AppUser();
        a.setId(1L);
        a.setUsername("ze");
        a.setPasswordHash("x");
        a.setRole(AppUserRole.USER);
        AppUser b = new AppUser();
        b.setId(2L);
        b.setUsername("ana");
        b.setPasswordHash("y");
        b.setRole(AppUserRole.ADMIN);
        when(appUserRepository.findAll()).thenReturn(List.of(a, b));

        var list = appUserService.listAll();

        assertThat(list).extracting(r -> r.username()).containsExactly("ana", "ze");
    }
}
