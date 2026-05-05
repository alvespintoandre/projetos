package br.com.projetos.service;

import br.com.projetos.domain.Member;
import br.com.projetos.dto.MemberCreateRequest;
import br.com.projetos.dto.MemberResponse;
import br.com.projetos.exception.ResourceNotFoundException;
import br.com.projetos.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class MemberExternalService {

    private final MemberRepository memberRepository;

    public MemberExternalService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional
    public MemberResponse create(MemberCreateRequest request) {
        Member entity = new Member();
        entity.setNome(request.nome());
        entity.setAtribuicao(request.atribuicao());
        entity.setCriadoEm(Instant.now());
        Member saved = memberRepository.save(entity);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public MemberResponse get(Long id) {
        return memberRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Membro não encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> listAll() {
        return memberRepository.findAll().stream().map(this::toResponse).toList();
    }

    private MemberResponse toResponse(Member m) {
        return new MemberResponse(m.getId(), m.getNome(), m.getAtribuicao(), m.getCriadoEm());
    }
}
