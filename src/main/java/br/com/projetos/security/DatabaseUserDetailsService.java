package br.com.projetos.security;

import br.com.projetos.domain.AppUser;
import br.com.projetos.repository.AppUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    public DatabaseUserDetailsService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser u = appUserRepository.findByUsername(username.trim())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
        return User.builder()
                .username(u.getUsername())
                .password(u.getPasswordHash())
                .roles(u.getRole().name())
                .build();
    }
}
