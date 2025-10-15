package com.agendademais.repositories;

import com.agendademais.entities.LogPostagem;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LogPostagemRepository extends JpaRepository<LogPostagem, Long>, JpaSpecificationExecutor<LogPostagem> {

    void deleteByAutorId(Long autorId);

    List<LogPostagem> findByOcorrenciaAtividadeId(Long ocorrenciaAtividadeId);

    @Query("SELECT DISTINCT l.tituloAtividade FROM LogPostagem l WHERE LOWER(l.tituloAtividade) LIKE LOWER(CONCAT('%', :term, '%')) ORDER BY l.tituloAtividade")
    List<String> findDistinctTituloAtividadeByTerm(@Param("term") String term);

    @Query("SELECT DISTINCT l.assuntoDivulgacao FROM LogPostagem l WHERE LOWER(l.assuntoDivulgacao) LIKE LOWER(CONCAT('%', :term, '%')) ORDER BY l.assuntoDivulgacao")
    List<String> findDistinctAssuntoDivulgacaoByTerm(@Param("term") String term);

    // Busca todos os autorIds presentes nos logs
    @Query("SELECT DISTINCT l.autorId FROM LogPostagem l WHERE l.autorId IS NOT NULL")
    List<Long> findDistinctAutorIds();
}
