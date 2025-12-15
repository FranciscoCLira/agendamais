package com.agendademais.repositories;

import com.agendademais.entities.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface LocalRepository extends JpaRepository<Local, Long> {

    List<Local> findByTipoLocalAndLocalPaiNomeLocal(Integer tipoLocal, String nomeLocalPai);

    // Alternativa para usar ID:
    List<Local> findByTipoLocalAndLocalPai(Integer tipoLocal, Local localPai);
    
    // Novos métodos que usam ID diretamente (evita lazy loading issues)
    @Query("SELECT l FROM Local l WHERE l.tipoLocal = :tipoLocal AND l.localPai.id = :paisId ORDER BY l.nomeLocal")
    List<Local> findEstadosByPaisId(@Param("tipoLocal") Integer tipoLocal, @Param("paisId") Long paisId);
    
    @Query("SELECT l FROM Local l WHERE l.tipoLocal = :tipoLocal AND l.localPai.id = :estadoId ORDER BY l.nomeLocal")
    List<Local> findCidadesByEstadoId(@Param("tipoLocal") Integer tipoLocal, @Param("estadoId") Long estadoId);

    Optional<Local> findByTipoLocalAndNomeLocalAndLocalPai(int tipoLocal, String nomeLocal, Local localPai);

    Optional<Local> findByTipoLocalAndNomeLocal(Integer tipoLocal, String nomeLocal);

    List<Local> findByTipoLocal(int tipoLocal);

    List<Local> findByTipoLocalAndLocalPaiId(int tipoLocal, Long idPai);

    Optional<Local> findByNomeLocalAndTipoLocal(String nomeLocal, int tipoLocal);

    List<Local> findByTipoLocalAndNomeLocalContainingIgnoreCase(int tipoLocal, String nomeLocal);

    List<Local> findByTipoLocalAndLocalPaiNomeLocalIgnoreCase(Integer tipoLocal, String nomeLocalPai);

    // --- Métodos para administração de locais ---

    // Contadores
    long countByRevisadoLocal(String revisadoLocal);

    long countByTipoLocal(int tipoLocal);

    // Buscas ordenadas
    List<Local> findByTipoLocalOrderByNomeLocal(int tipoLocal);

    List<Local> findByTipoLocalAndLocalPaiOrderByNomeLocal(int tipoLocal, Local localPai);

    // Versão com paginação para administração
    Page<Local> findByTipoLocalAndLocalPaiOrderByNomeLocal(int tipoLocal, Local localPai, Pageable pageable);

    // Busca por nome ignorando case e excluindo ID
    List<Local> findByTipoLocalAndNomeLocalIgnoreCaseAndIdNot(int tipoLocal, String nomeLocal, Long id);

    // Query customizada para filtros complexos na administração - SIMPLIFICADA
    @Query("SELECT l FROM Local l WHERE " +
            "(:tipoLocal IS NULL OR l.tipoLocal = :tipoLocal) AND " +
            "(:nomeLocal IS NULL OR :nomeLocal = '' OR UPPER(l.nomeLocal) LIKE UPPER(CONCAT('%', :nomeLocal, '%'))) AND "
            +
            "(:revisadoLocal IS NULL OR :revisadoLocal = '' OR l.revisadoLocal = :revisadoLocal)")
    Page<Local> findByFiltros(@Param("tipoLocal") Integer tipoLocal,
            @Param("nomeLocal") String nomeLocal,
            @Param("revisadoLocal") String revisadoLocal,
            Pageable pageable);

    // Busca todos os locais para filtro com acentos (implementação no service)
    @Query("SELECT l FROM Local l WHERE " +
            "(:tipoLocal IS NULL OR l.tipoLocal = :tipoLocal) AND " +
            "(:revisadoLocal IS NULL OR :revisadoLocal = '' OR l.revisadoLocal = :revisadoLocal)")
    List<Local> findForAccentFilter(@Param("tipoLocal") Integer tipoLocal,
            @Param("revisadoLocal") String revisadoLocal);

    // Método alternativo simples para filtro por tipo apenas
    Page<Local> findByTipoLocal(Integer tipoLocal, Pageable pageable);

    // Método de debug para verificar os dados
    @Query("SELECT COUNT(l) FROM Local l WHERE l.tipoLocal = :tipoLocal")
    long countByTipoLocalDebug(@Param("tipoLocal") Integer tipoLocal);

}
