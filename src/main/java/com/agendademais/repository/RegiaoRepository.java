package com.agendademais.repository;

import com.agendademais.entities.Regiao;
import com.agendademais.entities.Local;
import com.agendademais.entities.Instituicao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para Regiao
 */
@Repository
public interface RegiaoRepository extends JpaRepository<Regiao, Long> {

       /**
        * Encontra uma região pelo código
        */
       Optional<Regiao> findByCodRegiao(String codRegiao);

       /**
        * Encontra uma região pelo código e instituição
        */
       Optional<Regiao> findByCodRegiaoAndInstituicao(String codRegiao, Instituicao instituicao);

       boolean existsByCodRegiaoAndInstituicao(String codRegiao, Instituicao instituicao);

       /**
        * Lista todas as regiões de um país
        */
       List<Regiao> findByPais(Local pais);

       /**
        * Lista todas as regiões de um estado específico
        */
       List<Regiao> findByEstado(Local estado);

       /**
        * Lista regiões de um país e estado
        */
       List<Regiao> findByPaisAndEstado(Local pais, Local estado);

       /**
        * Busca regiões por padrão no nome
        */
       List<Regiao> findByNomeRegiaoContainingIgnoreCase(String nomeRegiao);

       /**
        * Lista todas as regiões ordenadas por país, estado e nome
        */
       @Query("SELECT r FROM Regiao r ORDER BY r.pais.nomeLocal, r.estado.nomeLocal, r.nomeRegiao")
       List<Regiao> findAllOrdenado();

       /**
        * Lista regiões por instituição
        */
       List<Regiao> findByInstituicaoOrderByPaisNomeLocalAscEstadoNomeLocalAscNomeRegiaoAsc(Instituicao instituicao);

       /**
        * Verifica se uma cidade pertence à região via JOIN sem carregar coleção lazy
        */
       @Query("SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END " +
                     "FROM Regiao r JOIN r.cidades c " +
                     "WHERE r.id = :regiaoId AND c.id = :cidadeId")
       boolean existsCidadeInRegiao(@org.springframework.data.repository.query.Param("regiaoId") Long regiaoId,
                     @org.springframework.data.repository.query.Param("cidadeId") Long cidadeId);
}
