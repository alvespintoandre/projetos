package br.com.projetos.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public class PortfolioReportRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("unchecked")
    public List<Object[]> aggregateProjectsByStatus() {
        return entityManager.createNativeQuery("""
                        SELECT status,
                               CAST(COUNT(*) AS BIGINT),
                               COALESCE(SUM(orcamento_total), 0)
                        FROM projects
                        GROUP BY status
                        ORDER BY status
                        """)
                .getResultList();
    }

    public long countDistinctAllocatedMembers() {
        Object single = entityManager.createNativeQuery("""
                        SELECT COUNT(DISTINCT member_id)
                        FROM project_members
                        """)
                .getSingleResult();
        if (single instanceof Number n) {
            return n.longValue();
        }
        return 0L;
    }

    public BigDecimal averageDurationDaysEncerrados() {
        Object result = entityManager.createNativeQuery("""
                        SELECT AVG(data_real_termino - data_inicio)
                        FROM projects
                        WHERE status = 'ENCERRADO'
                          AND data_real_termino IS NOT NULL
                          AND data_inicio IS NOT NULL
                        """)
                .getSingleResult();
        if (result == null) {
            return BigDecimal.ZERO;
        }
        if (result instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        return BigDecimal.ZERO;
    }
}
