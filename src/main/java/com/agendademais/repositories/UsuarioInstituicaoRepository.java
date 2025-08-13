package com.agendademais.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.agendademais.entities.Instituicao;
import com.agendademais.entities.Usuario;
import com.agendademais.entities.UsuarioInstituicao;

import jakarta.transaction.Transactional;

@Repository
public interface UsuarioInstituicaoRepository extends JpaRepository<UsuarioInstituicao, Long> {

	// checar se existem vínculos 
    boolean existsByUsuarioId(Long usuarioId);
    
    boolean existsByUsuarioIdAndInstituicaoId(Long usuarioId, Long instituicaoId);

    Optional<UsuarioInstituicao> findByUsuarioIdAndInstituicaoId(Long usuarioId, Long instituicaoId);

    List<UsuarioInstituicao> findByUsuarioIdAndSitAcessoUsuarioInstituicao(Long usuarioId, String sit);
    
    List<UsuarioInstituicao> findByUsuario(Usuario usuario);
    
    List<UsuarioInstituicao> findByUsuarioId(Long usuarioId);
    
    // Buscar usuários por instituição ordenados por nível de acesso
    List<UsuarioInstituicao> findByInstituicaoOrderByNivelAcessoUsuarioInstituicaoAsc(Instituicao instituicao);
    
    // Buscar usuários por instituição com nível de acesso menor ou igual ao especificado
    List<UsuarioInstituicao> findByInstituicaoAndNivelAcessoUsuarioInstituicaoLessThanEqualOrderByNivelAcessoUsuarioInstituicaoAsc(
            Instituicao instituicao, Integer nivelMaximo);
    
    // Buscar todos os usuários (para controle total)
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

//    @Query("SELECT ui.instituicao FROM UsuarioInstituicao ui " +
//  	       "WHERE ui.usuario.id = :idUsuario AND ui.sitAcessoUsuarioInstituicao = 'A' " +
//  	       "AND ui.instituicao.situacaoInstituicao = 'A'")
//     List<Instituicao> findInstituicoesAtivasPorUsuario(@Past("idUsuario") Long idUsuario);
  
    
    
    // apagar todos os vínculos de um usuário
    void deleteAllByUsuarioId(Long usuarioId);
    
    void deleteAllByUsuario(Usuario usuario);
    
    @Transactional
    @ModelAttribute 
    void deleteByUsuarioAndInstituicao(Usuario usuario, Instituicao instituicao);
}        
