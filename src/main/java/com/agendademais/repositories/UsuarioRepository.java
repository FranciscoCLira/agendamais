package com.agendademais.repositories;

import com.agendademais.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @Query("SELECT MAX(u.username) FROM Usuario u WHERE u.username LIKE CONCAT(:prefixo, '%')")
    String findMaxUsernameStartingWith(@Param("prefixo") String prefixo);
    
    Optional<Usuario> findByUsername(String username);
    
    boolean existsByUsername(String username);

    Optional<Usuario> findByUsernameAndPassword(String username, String password);

    List<Usuario> findAllByPessoaEmailPessoa(String email);
    
    @Query("SELECT u FROM Usuario u WHERE LOWER(u.pessoa.emailPessoa) = LOWER(:email)")
    Optional<Usuario> findByEmailPessoa(@Param("email") String email);
    
    Optional<Usuario> findByTokenRecuperacao(String tokenRecuperacao);

    
    
}        
