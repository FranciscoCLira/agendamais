package com.agendademais.repositories;

import com.agendademais.entities.Instituicao;
import com.agendademais.entities.Pessoa;
import com.agendademais.entities.PessoaSubInstituicao;
import com.agendademais.entities.SubInstituicao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PessoaSubInstituicaoRepository extends JpaRepository<PessoaSubInstituicao, Long> {

	void deleteByPessoa(Pessoa pessoa);

	void deleteAllByPessoaId(Long pessoaId);

	void deleteByPessoaAndInstituicao(Pessoa pessoa, Instituicao instituicao);

	void deleteByPessoaAndInstituicaoAndSubInstituicaoIn(Pessoa pessoa, Instituicao instituicao,
			List<PessoaSubInstituicao> subInstituicoes);

	List<PessoaSubInstituicao> findByPessoa(Pessoa pessoa);
	
	// Método para buscar qualquer vínculo de sub-instituição de uma pessoa
	Optional<PessoaSubInstituicao> findFirstByPessoaId(Long pessoaId);
	
	// Novos métodos para CRUD de Sub-Instituições
	boolean existsByPessoaAndSubInstituicao(Pessoa pessoa, SubInstituicao subInstituicao);
	
	// Método para buscar vínculo de uma pessoa em uma instituição específica
	Optional<PessoaSubInstituicao> findByPessoaAndInstituicao(Pessoa pessoa, Instituicao instituicao);
}
