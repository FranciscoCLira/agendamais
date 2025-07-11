package com.agendademais.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.agendademais.entities.UsuarioInstituicao;

@Repository
public interface UsuarioInstituicaoRepository extends JpaRepository<UsuarioInstituicao, Long> {

	// checar se existem vínculos 
    boolean existsByUsuarioId(Long usuarioId);
    
    boolean existsByUsuarioIdAndInstituicaoId(Long usuarioId, Long instituicaoId);

    List<UsuarioInstituicao> findByUsuarioIdAndSitAcessoUsuarioInstituicao(Long usuarioId, String sit);

    Optional<UsuarioInstituicao> findByUsuarioIdAndInstituicaoId(Long usuarioId, Long instituicaoId);
    
    List<UsuarioInstituicao> findByUsuarioId(Long usuarioId);
    
    // apagar todos os vínculos de um usuário
    void deleteAllByUsuarioId(Long usuarioId);


}        
