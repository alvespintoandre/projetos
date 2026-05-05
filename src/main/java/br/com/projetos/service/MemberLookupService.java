package br.com.projetos.service;

import br.com.projetos.domain.Member;
import br.com.projetos.domain.MemberRole;
import br.com.projetos.exception.BusinessRuleException;
import br.com.projetos.exception.ResourceNotFoundException;
import br.com.projetos.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Service
public class MemberLookupService {

    private final MemberRepository memberRepository;

    public MemberLookupService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member require(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membro não encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public Set<Member> requireFuncionarios(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessRuleException("É necessário alocar entre 1 e 10 membros com atribuição funcionário.");
        }
        if (ids.size() > 10) {
            throw new BusinessRuleException("Cada projeto pode ter no máximo 10 membros alocados.");
        }
        Set<Long> unique = new HashSet<>(ids);
        if (unique.size() != ids.size()) {
            throw new BusinessRuleException("Lista de membros alocados contém duplicados.");
        }
        Set<Member> membros = new HashSet<>();
        for (Long mid : ids) {
            Member m = require(mid);
            if (m.getAtribuicao() != MemberRole.FUNCIONARIO) {
                throw new BusinessRuleException(
                        "Apenas membros com atribuição funcionário podem ser alocados ao projeto. Id: " + mid);
            }
            membros.add(m);
        }
        return membros;
    }

    public void validateProjectCapForMembers(Set<Member> membros, Long excludeProjectId) {
        for (Member m : membros) {
            long count = excludeProjectId == null
                    ? memberRepository.countActiveProjectsForMember(
                    m.getId(),
                    br.com.projetos.domain.ProjectStatus.ENCERRADO,
                    br.com.projetos.domain.ProjectStatus.CANCELADO)
                    : memberRepository.countActiveProjectsForMemberExcluding(
                    m.getId(),
                    excludeProjectId,
                    br.com.projetos.domain.ProjectStatus.ENCERRADO,
                    br.com.projetos.domain.ProjectStatus.CANCELADO);
            if (count >= 3) {
                throw new BusinessRuleException(
                        "Membro " + m.getId() + " já está em 3 projetos ativos (não encerrados/cancelados).");
            }
        }
    }
}
