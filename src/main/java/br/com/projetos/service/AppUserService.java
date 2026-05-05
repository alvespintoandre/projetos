package br.com.projetos.service;

import br.com.projetos.domain.AppUser;
import br.com.projetos.domain.AppUserRole;
import br.com.projetos.dto.AppUserCreateRequest;
import br.com.projetos.dto.AppUserResponse;
import br.com.projetos.exception.BusinessRuleException;
import br.com.projetos.exception.ResourceNotFoundException;
import br.com.projetos.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<AppUserResponse> listAll() {
        return appUserRepository.findAll().stream()
                .map(this::toResponse)
                .sorted((a, b) -> a.username().compareToIgnoreCase(b.username()))
                .toList();
    }

    @Transactional(readOnly = true)
    public AppUserResponse getById(Long id) {
        return appUserRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário de aplicação não encontrado: " + id));
    }

    @Transactional
    public AppUserResponse create(AppUserCreateRequest request) {
        if (appUserRepository.existsByUsername(request.username().trim())) {
            throw new BusinessRuleException("Já existe usuário com o login: " + request.username().trim());
        }
        AppUser u = new AppUser();
        u.setUsername(request.username().trim());
        u.setPasswordHash(passwordEncoder.encode(request.password()));
        u.setRole(request.role());
        return toResponse(appUserRepository.save(u));
    }

    private AppUserResponse toResponse(AppUser u) {
        return new AppUserResponse(u.getId(), u.getUsername(), u.getRole());
    }
}
