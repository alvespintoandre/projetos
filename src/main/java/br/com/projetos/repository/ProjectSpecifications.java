package br.com.projetos.repository;

import br.com.projetos.domain.Project;
import br.com.projetos.domain.ProjectStatus;
import org.springframework.data.jpa.domain.Specification;

public final class ProjectSpecifications {

    private ProjectSpecifications() {
    }

    public static Specification<Project> nomeContains(String nome) {
        if (nome == null || nome.isBlank()) {
            return (root, query, cb) -> cb.conjunction();
        }
        String pattern = "%" + nome.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("nome")), pattern);
    }

    public static Specification<Project> statusEquals(ProjectStatus status) {
        if (status == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Project> combine(String nome, ProjectStatus status) {
        return Specification.where(nomeContains(nome)).and(statusEquals(status));
    }
}
