package com.agendademais.repositories;

import com.agendademais.entities.Atividade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface AtividadeRepository extends JpaRepository<Atividade, Long>, JpaSpecificationExecutor<Atividade> {
    List<Atividade> findByTituloAtividadeContainingIgnoreCase(String titulo);

    void deleteByIdSolicitante(com.agendademais.entities.Pessoa idSolicitante);

    boolean existsByIdSolicitante(com.agendademais.entities.Pessoa idSolicitante);

    // Buscar solicitantes distintos de atividades de uma instituição
    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT a.idSolicitante FROM Atividade a WHERE a.instituicao.id = :instituicaoId AND a.idSolicitante IS NOT NULL")
    List<com.agendademais.entities.Pessoa> findDistinctSolicitantesByInstituicaoId(Long instituicaoId);

    // Buscar atividades por título e instituição (para autocomplete)
    @org.springframework.data.jpa.repository.Query("SELECT a FROM Atividade a WHERE a.instituicao.id = :instituicaoId AND LOWER(a.tituloAtividade) LIKE LOWER(CONCAT('%', :titulo, '%'))")
    List<Atividade> findByTituloAtividadeAndInstituicaoId(String titulo, Long instituicaoId);
}
