package br.com.projetos.repository;

import br.com.projetos.domain.Project;
import br.com.projetos.domain.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {

    @EntityGraph(value = "Project.details", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT p FROM Project p WHERE p.id = :id")
    Optional<Project> findWithDetailsById(@Param("id") Long id);

    @EntityGraph(value = "Project.details", type = EntityGraph.EntityGraphType.LOAD)
    Page<Project> findAll(Specification<Project> spec, Pageable pageable);

    List<Project> findByStatus(ProjectStatus status);

    @Query("""
            SELECT p FROM Project p
            WHERE p.status = ENCERRADO
              AND p.dataRealTermino IS NOT NULL
            """)
    List<Project> findEncerradosComDataReal();
}
