package com.agendademais.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class TemaOcorrenciaRepositoryCustomImpl implements TemaOcorrenciaRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public java.util.List<String> findDistinctTemaOcorrenciaByTerm(String term) {
        return entityManager.createQuery(
                "SELECT DISTINCT o.temaOcorrencia FROM OcorrenciaAtividade o WHERE LOWER(o.temaOcorrencia) LIKE LOWER(CONCAT('%', :term, '%')) ORDER BY o.temaOcorrencia",
                String.class)
                .setParameter("term", term)
                .setMaxResults(10)
                .getResultList();
    }
}
