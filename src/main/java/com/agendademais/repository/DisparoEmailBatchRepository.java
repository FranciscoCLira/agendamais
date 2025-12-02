package com.agendademais.repository;

import com.agendademais.model.DisparoEmailBatch;
import com.agendademais.model.DisparoEmailBatch.StatusDisparo;
import com.agendademais.entities.Instituicao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para DisparoEmailBatch.
 */
@Repository
public interface DisparoEmailBatchRepository extends JpaRepository<DisparoEmailBatch, Long> {

    /**
     * Busca disparos por instituição ordenados por data de criação.
     */
    List<DisparoEmailBatch> findByInstituicaoOrderByDataCriacaoDesc(Instituicao instituicao);

    /**
     * Busca disparos por instituição e status.
     */
    List<DisparoEmailBatch> findByInstituicaoAndStatusOrderByDataCriacaoDesc(
        Instituicao instituicao, 
        StatusDisparo status
    );

    /**
     * Busca disparos pendentes ou em processamento.
     */
    List<DisparoEmailBatch> findByStatusIn(List<StatusDisparo> statuses);

    /**
     * Busca disparos pendentes ordenados por data de criação.
     */
    List<DisparoEmailBatch> findByStatusOrderByDataCriacaoAsc(StatusDisparo status);
}
