package com.agendademais.repositories;

import com.agendademais.entities.UsuarioInstituicao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioInstituicaoRepository extends JpaRepository<UsuarioInstituicao, Long> {

    Optional<UsuarioInstituicao> findByUsuarioIdAndInstituicaoId(Long usuarioId, Long instituicaoId);
    
    List<UsuarioInstituicao> findByUsuarioId(Long usuarioId);
    
    boolean existsByUsuarioIdAndInstituicaoId(Long usuarioId, Long instituicaoId);

}        
