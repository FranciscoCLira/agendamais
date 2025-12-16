package com.agendademais.repositories;

import com.agendademais.entities.Usuario;
import com.agendademais.entities.Instituicao;
import com.agendademais.entities.Local;
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

       /**
        * Busca todos os usuários vinculados a uma instituição.
        * Usado para disparos de email que atingem TODOS os usuários da instituição.
        * 
        * @param instituicao a instituição para filtrar
        * @return lista de usuários da instituição com email válido
        */
       @Query("SELECT DISTINCT u FROM Usuario u " +
                     "JOIN UsuarioInstituicao ui ON u.id = ui.usuario.id " +
                     "WHERE ui.instituicao = :instituicao " +
                     "AND u.pessoa IS NOT NULL " +
                     "AND u.pessoa.emailPessoa IS NOT NULL " +
                     "AND u.pessoa.emailPessoa <> '' " +
                     "ORDER BY u.username ASC")
       List<Usuario> findByInstituicao(@Param("instituicao") Instituicao instituicao);

       /**
        * Busca usuários vinculados a uma instituição cujas cidades pertencem a uma
        * região.
        * Usado para disparos de email filtrados por região.
        * 
        * @param instituicao a instituição para filtrar
        * @param cidades     lista de cidades da região
        * @return lista de usuários da instituição com cidades na região especificada
        */
       @Query("SELECT DISTINCT u FROM Usuario u " +
                     "JOIN UsuarioInstituicao ui ON u.id = ui.usuario.id " +
                     "WHERE ui.instituicao = :instituicao " +
                     "AND u.pessoa IS NOT NULL " +
                     "AND u.pessoa.emailPessoa IS NOT NULL " +
                     "AND u.pessoa.emailPessoa <> '' " +
                     "AND u.pessoa.cidade IN (:cidades) " +
                     "ORDER BY u.username ASC")
       List<Usuario> findByInstituicaoAndCidadeIn(
                     @Param("instituicao") Instituicao instituicao,
                     @Param("cidades") List<Local> cidades);

}
