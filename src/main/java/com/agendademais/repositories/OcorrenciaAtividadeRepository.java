package com.agendademais.repositories;

import com.agendademais.entities.Atividade;
import com.agendademais.entities.OcorrenciaAtividade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface OcorrenciaAtividadeRepository
        extends JpaRepository<OcorrenciaAtividade, Long>, JpaSpecificationExecutor<OcorrenciaAtividade> {
    Page<OcorrenciaAtividade> findByIdAtividade(Atividade idAtividade, Pageable pageable);

    @Query("SELECT DISTINCT o.temaOcorrencia FROM OcorrenciaAtividade o WHERE LOWER(o.temaOcorrencia) LIKE LOWER(CONCAT('%', :term, '%')) ORDER BY o.temaOcorrencia")
    List<String> findDistinctTemaOcorrenciaByTerm(@Param("term") String term);
}
