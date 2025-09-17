package com.agendademais.repositories;

import com.agendademais.entities.LogPostagem;
import org.springframework.data.jpa.repository.JpaRepository;
import com.agendademais.entities.Autor;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface LogPostagemRepository extends JpaRepository<LogPostagem, Long> {
    @Query("SELECT DISTINCT l.tituloAtividade FROM LogPostagem l WHERE LOWER(l.tituloAtividade) LIKE LOWER(CONCAT('%', :term, '%')) ORDER BY l.tituloAtividade")
    List<String> findDistinctTituloAtividadeByTerm(@Param("term") String term);

    @Query("SELECT DISTINCT l.assuntoDivulgacao FROM LogPostagem l WHERE LOWER(l.assuntoDivulgacao) LIKE LOWER(CONCAT('%', :term, '%')) ORDER BY l.assuntoDivulgacao")
    List<String> findDistinctAssuntoDivulgacaoByTerm(@Param("term") String term);

    // Busca nomes de pessoas vinculadas aos autores presentes nos logs
    @Query("SELECT DISTINCT CONCAT(p.nomePessoa, ' <', p.emailPessoa, '>') FROM LogPostagem l JOIN Autor a ON l.autorId = a.id JOIN a.pessoa p WHERE LOWER(p.nomePessoa) LIKE LOWER(CONCAT('%', :term, '%')) OR LOWER(p.emailPessoa) LIKE LOWER(CONCAT('%', :term, '%')) ORDER BY p.nomePessoa")
    List<String> findDistinctAutorNomeOuEmailByTerm(@Param("term") String term);
}
