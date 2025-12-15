// Exclusão em massa por usuário ou vínculo deve estar dentro da interface abaixo
package com.agendademais.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.agendademais.entities.Instituicao;
import com.agendademais.entities.Usuario;
import com.agendademais.entities.UsuarioInstituicao;

@Repository
public interface UsuarioInstituicaoRepository extends JpaRepository<UsuarioInstituicao, Long> {

        java.util.Optional<UsuarioInstituicao> findByUsuarioAndInstituicao(com.agendademais.entities.Usuario usuario,
                        com.agendademais.entities.Instituicao instituicao);

        long countByInstituicaoId(Long instituicaoId);

        // checar se existem vínculos
        boolean existsByUsuarioId(Long usuarioId);

        boolean existsByUsuarioIdAndInstituicaoId(Long usuarioId, Long instituicaoId);

        Optional<UsuarioInstituicao> findByUsuarioIdAndInstituicaoId(Long usuarioId, Long instituicaoId);

        List<UsuarioInstituicao> findByUsuarioIdAndSitAcessoUsuarioInstituicao(Long usuarioId, String sit);

        List<UsuarioInstituicao> findByUsuario(Usuario usuario);

        List<UsuarioInstituicao> findByUsuarioId(Long usuarioId);

        // Buscar vínculos por instituição e pessoa do usuário
        List<UsuarioInstituicao> findByInstituicaoAndUsuario_Pessoa(Instituicao instituicao, com.agendademais.entities.Pessoa pessoa);

        // Buscar usuários por instituição ordenados por nível de acesso (paginação)
        Page<UsuarioInstituicao> findByInstituicaoOrderByNivelAcessoUsuarioInstituicaoAsc(Instituicao instituicao,
                        Pageable pageable);

        // Versão antiga sem paginação (mantida para compatibilidade)
        List<UsuarioInstituicao> findByInstituicaoOrderByNivelAcessoUsuarioInstituicaoAsc(Instituicao instituicao);

        // Buscar usuários por instituição com nível de acesso menor ou igual ao
        // especificado
        List<UsuarioInstituicao> findByInstituicaoAndNivelAcessoUsuarioInstituicaoLessThanEqualOrderByNivelAcessoUsuarioInstituicaoAsc(
                        Instituicao instituicao, Integer nivelMaximo);

        // Paginated search by institution and filter (name, username, email)
        @Query("SELECT ui FROM UsuarioInstituicao ui " +
                        "JOIN ui.usuario u " +
                        "JOIN u.pessoa p " +
                        "WHERE ui.instituicao = :instituicao " +
                        "AND (LOWER(u.username) LIKE LOWER(CONCAT('%', :filtro, '%')) " +
                        "     OR LOWER(p.nomePessoa) LIKE LOWER(CONCAT('%', :filtro, '%')) " +
                        "     OR LOWER(p.emailPessoa) LIKE LOWER(CONCAT('%', :filtro, '%'))) " +
                        "ORDER BY ui.nivelAcessoUsuarioInstituicao ASC")
        Page<UsuarioInstituicao> findByInstituicaoAndFiltro(
                        @Param("instituicao") Instituicao instituicao,
                        @Param("filtro") String filtro,
                        Pageable pageable);

        @Query("SELECT ui FROM UsuarioInstituicao ui " +
                        "JOIN FETCH ui.usuario u " +
                        "JOIN FETCH u.pessoa p " +
                        "JOIN FETCH ui.instituicao i " +
                        "ORDER BY i.nomeInstituicao, ui.nivelAcessoUsuarioInstituicao")
        List<UsuarioInstituicao> findAllWithDetails();

        @Query("SELECT ui.instituicao FROM UsuarioInstituicao ui " +
                        "WHERE ui.usuario.id = :usuarioId " +
                        "AND ui.sitAcessoUsuarioInstituicao = 'A' " +
                        "AND ui.instituicao.situacaoInstituicao = 'A'")
        List<Instituicao> findInstituicoesAtivasPorUsuario(Long usuarioId);

        // Busca avançada com múltiplos filtros (todos opcionais)
        @Query("SELECT ui FROM UsuarioInstituicao ui " +
                        "JOIN ui.usuario u " +
                        "JOIN u.pessoa p " +
                        "LEFT JOIN p.pais pais " +
                        "LEFT JOIN p.estado estado " +
                        "LEFT JOIN p.cidade cidade " +
                        "LEFT JOIN p.pessoaSubInstituicao psi " +
                        "LEFT JOIN psi.subInstituicao subInst " +
                        "WHERE ui.instituicao = :instituicao " +
                        "AND (:username IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))) " +
                        "AND (:nome IS NULL OR LOWER(p.nomePessoa) LIKE LOWER(CONCAT('%', :nome, '%'))) " +
                        "AND (:email IS NULL OR LOWER(p.emailPessoa) LIKE LOWER(CONCAT('%', :email, '%'))) " +
                        "AND (:subInstituicao IS NULL OR LOWER(subInst.nomeSubInstituicao) LIKE LOWER(CONCAT('%', :subInstituicao, '%'))) "
                        +
                        "AND (:situacao IS NULL OR ui.sitAcessoUsuarioInstituicao = :situacao) " +
                        "AND (:pais IS NULL OR LOWER(pais.nomeLocal) LIKE LOWER(CONCAT('%', :pais, '%'))) " +
                        "AND (:estado IS NULL OR LOWER(estado.nomeLocal) LIKE LOWER(CONCAT('%', :estado, '%'))) " +
                        "AND (:cidade IS NULL OR LOWER(cidade.nomeLocal) LIKE LOWER(CONCAT('%', :cidade, '%'))) " +
                        "ORDER BY ui.nivelAcessoUsuarioInstituicao ASC")
        Page<UsuarioInstituicao> buscaAvancada(
                        @Param("instituicao") Instituicao instituicao,
                        @Param("username") String username,
                        @Param("nome") String nome,
                        @Param("email") String email,
                        @Param("subInstituicao") String subInstituicao,
                        @Param("situacao") String situacao,
                        @Param("pais") String pais,
                        @Param("estado") String estado,
                        @Param("cidade") String cidade,
                        Pageable pageable);

        // Métodos de exclusão em massa e por vínculo
        void deleteAllByUsuarioId(Long usuarioId);

        void deleteAllByUsuario(Usuario usuario);

        void deleteByUsuarioAndInstituicao(Usuario usuario, Instituicao instituicao);
}
