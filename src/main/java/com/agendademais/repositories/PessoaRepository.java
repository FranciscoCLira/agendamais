package com.agendademais.repositories;

import com.agendademais.entities.Local;
import com.agendademais.entities.Pessoa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PessoaRepository extends JpaRepository<Pessoa, Long> {

	// Para autocomplete eficiente
	List<Pessoa> findTop10ByNomePessoaContainingIgnoreCaseOrEmailPessoaContainingIgnoreCase(String nome, String email);

	Optional<Pessoa> findByEmailPessoa(String emailPessoa);

	List<Pessoa> findAllByEmailPessoa(String emailPessoa);

	// Métodos para buscar pessoas por local
	List<Pessoa> findByPais(Local pais);

	List<Pessoa> findByEstado(Local estado);

	List<Pessoa> findByCidade(Local cidade);

}
