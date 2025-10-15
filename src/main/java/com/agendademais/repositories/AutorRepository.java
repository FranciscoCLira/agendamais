
package com.agendademais.repositories;

import com.agendademais.entities.Autor;
import com.agendademais.entities.Pessoa;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AutorRepository extends JpaRepository<Autor, Long> {

    Optional<Autor> findByPessoa(Pessoa pessoa);

    // Lista autores cuja pessoa está vinculada a um usuário com vínculo ativo na
    // instituição
    @Query("SELECT a FROM Autor a WHERE EXISTS ("
            + "SELECT 1 FROM Usuario u, UsuarioInstituicao ui "
            + "WHERE u.pessoa.id = a.pessoa.id "
            + "AND ui.usuario.id = u.id "
            + "AND ui.sitAcessoUsuarioInstituicao = 'A' "
            + "AND ui.instituicao.id = :instituicaoId"
            + ")")
    java.util.List<Autor> findAutoresVinculadosAtivosPorInstituicao(@Param("instituicaoId") Long instituicaoId);

}
