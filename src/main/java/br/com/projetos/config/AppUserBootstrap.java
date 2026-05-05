package br.com.projetos.config;

import br.com.projetos.domain.AppUser;
import br.com.projetos.domain.AppUserRole;
import br.com.projetos.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(1)
public class AppUserBootstrap implements ApplicationRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;

    public AppUserBootstrap(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.security.bootstrap.admin-username:admin}") String adminUsername,
            @Value("${app.security.bootstrap.admin-password:admin123}") String adminPassword) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (appUserRepository.count() > 0) {
            return;
        }
        AppUser admin = new AppUser();
        admin.setUsername(adminUsername.trim());
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setRole(AppUserRole.ADMIN);
        appUserRepository.save(admin);
    }
}
