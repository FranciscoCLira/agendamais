package com.agendademais.repositories;

import com.agendademais.entities.SubInstituicao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SubInstituicaoRepository extends JpaRepository<SubInstituicao, Long> {

	// Object findBySituacaoSubInstituicao(String situacao);
	
	List<SubInstituicao> findBySituacaoSubInstituicao(String situacao);

}
