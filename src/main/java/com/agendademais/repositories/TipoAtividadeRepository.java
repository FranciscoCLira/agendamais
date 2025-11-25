package com.agendademais.repositories;

import com.agendademais.entities.Instituicao;
import com.agendademais.entities.TipoAtividade;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoAtividadeRepository extends JpaRepository<TipoAtividade, Long> {
	long countByInstituicaoId(Long instituicaoId);

	List<TipoAtividade> findByInstituicao(Instituicao instituicao);

	List<TipoAtividade> findByInstituicaoId(Long instituicao);

	// Busca tipos de atividade por instituição e título contendo termo (case
	// insensitive)
	List<TipoAtividade> findByInstituicaoIdAndTituloTipoAtividadeContainingIgnoreCase(Long instituicaoId,
			String titulo);
}
