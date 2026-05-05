package br.com.projetos.repository;

import br.com.projetos.domain.Member;
import br.com.projetos.domain.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("""
            SELECT COUNT(DISTINCT p.id)
            FROM Project p
            JOIN p.membrosAlocados m
            WHERE m.id = :memberId
              AND p.status NOT IN (:encerrado, :cancelado)
            """)
    long countActiveProjectsForMember(
            @Param("memberId") Long memberId,
            @Param("encerrado") ProjectStatus encerrado,
            @Param("cancelado") ProjectStatus cancelado
    );

    @Query("""
            SELECT COUNT(DISTINCT p.id)
            FROM Project p
            JOIN p.membrosAlocados m
            WHERE m.id = :memberId
              AND p.id <> :excludeProjectId
              AND p.status NOT IN (:encerrado, :cancelado)
            """)
    long countActiveProjectsForMemberExcluding(
            @Param("memberId") Long memberId,
            @Param("excludeProjectId") Long excludeProjectId,
            @Param("encerrado") ProjectStatus encerrado,
            @Param("cancelado") ProjectStatus cancelado
    );
}
