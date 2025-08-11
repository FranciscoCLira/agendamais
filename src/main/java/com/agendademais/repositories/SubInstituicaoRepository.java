package com.agendademais.repositories;

import com.agendademais.entities.SubInstituicao;
import com.agendademais.entities.Instituicao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SubInstituicaoRepository extends JpaRepository<SubInstituicao, Long> {

	// Object findBySituacaoSubInstituicao(String situacao);
	
	List<SubInstituicao> findBySituacaoSubInstituicao(String situacao);
	
	// Método para autocomplete de sub-instituições
	List<SubInstituicao> findByNomeSubInstituicaoContainingIgnoreCaseAndSituacaoSubInstituicao(String nome, String situacao);
	
	// Método para buscar sub-instituição por nome e instituição
	List<SubInstituicao> findByNomeSubInstituicaoAndInstituicao(String nome, Instituicao instituicao);
}
