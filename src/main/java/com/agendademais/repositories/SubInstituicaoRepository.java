package com.agendademais.repositories;

import com.agendademais.entities.SubInstituicao;
import com.agendademais.entities.Instituicao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SubInstituicaoRepository extends JpaRepository<SubInstituicao, Long> {

	List<SubInstituicao> findBySituacaoSubInstituicao(String situacao);

	List<SubInstituicao> findByNomeSubInstituicaoContainingIgnoreCaseAndSituacaoSubInstituicao(String nome,
			String situacao);

	List<SubInstituicao> findByNomeSubInstituicaoAndInstituicao(String nome, Instituicao instituicao);

	// Novo método para buscar sub-instituições ativas por instituição
	List<SubInstituicao> findByInstituicaoAndSituacaoSubInstituicao(Instituicao instituicao, String situacao);

	// Conta quantas subinstituições existem para uma instituição
	long countByInstituicaoId(Long instituicaoId);

	// Novo método para buscar todas as sub-instituições de uma instituição,
	// independentemente da situação
	List<SubInstituicao> findByInstituicao(Instituicao instituicao);
	
	// Busca sub-instituições por instituição (ID)
	List<SubInstituicao> findByInstituicaoId(Long instituicaoId);
	
	// Busca sub-instituições por instituição e nome contendo termo (case insensitive)
	List<SubInstituicao> findByInstituicaoIdAndNomeSubInstituicaoContainingIgnoreCase(Long instituicaoId, String nome);
}
