package com.agendademais.repositories;

import com.agendademais.entities.Autor;
import com.agendademais.entities.Pessoa;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AutorRepository extends JpaRepository<Autor, Long> {

    Optional<Autor> findByIdPessoa(Pessoa pessoa);

}
