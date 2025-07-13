package com.agendademais.repositories;


import com.agendademais.entities.Pessoa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PessoaRepository extends JpaRepository<Pessoa, Long> {
	
	Optional<Pessoa> findByEmailPessoa(String emailPessoa);
	
	List<Pessoa> findAllByEmailPessoa(String emailPessoa);

}
