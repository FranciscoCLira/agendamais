package com.agendademais.repositories;

import com.agendademais.entities.Instituicao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InstituicaoRepository extends JpaRepository<Instituicao, Long> {

	// Object findBySituacaoInstituicao(String situacao);
	
	List<Instituicao> findBySituacaoInstituicao(String situacao);
}
