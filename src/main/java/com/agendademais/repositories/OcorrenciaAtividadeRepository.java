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
    @Query("SELECT DISTINCT o.idAutor FROM OcorrenciaAtividade o WHERE (:atividadeId IS NULL OR o.idAtividade.id = :atividadeId) AND (LOWER(o.idAutor.pessoa.nomePessoa) LIKE LOWER(CONCAT('%', :term, '%')) OR LOWER(o.idAutor.pessoa.emailPessoa) LIKE LOWER(CONCAT('%', :term, '%'))) AND o.idAutor IS NOT NULL")
    List<com.agendademais.entities.Autor> findDistinctAutoresByTermAndAtividadeId(@Param("term") String term,
            @Param("atividadeId") Long atividadeId);

    @Query("SELECT DISTINCT o.idAutor.id FROM OcorrenciaAtividade o WHERE o.idAtividade.id = :atividadeId AND o.idAutor IS NOT NULL")
    List<Long> findDistinctAutorIdsByAtividadeId(@Param("atividadeId") Long atividadeId);

    @Query("SELECT DISTINCT o.idAutor.id FROM OcorrenciaAtividade o WHERE o.idAutor IS NOT NULL")
    List<Long> findDistinctAutorIdsComOcorrencia();

    Page<OcorrenciaAtividade> findByIdAtividade(Atividade idAtividade, Pageable pageable);

    @Query("SELECT DISTINCT o.temaOcorrencia FROM OcorrenciaAtividade o WHERE LOWER(o.temaOcorrencia) LIKE LOWER(CONCAT('%', :term, '%')) ORDER BY o.temaOcorrencia")
    List<String> findDistinctTemaOcorrenciaByTerm(@Param("term") String term);
}
