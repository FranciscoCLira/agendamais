package com.agendademais.repositories;

import com.agendademais.entities.PessoaInstituicao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PessoaInstituicaoRepository extends JpaRepository<PessoaInstituicao, Long> {
	
	boolean existsByPessoaId(Long pessoaId);
}
