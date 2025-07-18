package com.agendademais.repositories;

import com.agendademais.entities.Instituicao;
import com.agendademais.entities.Pessoa;
import com.agendademais.entities.PessoaSubInstituicao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PessoaSubInstituicaoRepository extends JpaRepository<PessoaSubInstituicao, Long> {
	
	void deleteByPessoa(Pessoa pessoa);
	
	void deleteAllByPessoaId(Long pessoaId);

	void deleteByPessoaAndInstituicao(Pessoa pessoa, Instituicao instituicao);
	
	void deleteByPessoaAndInstituicaoAndSubInstituicaoIn(Pessoa pessoa, Instituicao instituicao, List<PessoaSubInstituicao> subInstituicoes);
}
