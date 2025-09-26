package com.agendademais.repositories;

import com.agendademais.entities.Instituicao;
import com.agendademais.entities.Pessoa;
import com.agendademais.entities.PessoaInstituicao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PessoaInstituicaoRepository extends JpaRepository<PessoaInstituicao, Long> {
    long countByInstituicaoId(Long instituicaoId);

    boolean existsByPessoaId(Long pessoaId);

    void deleteAllByPessoaId(Long pessoaId);

    void deleteByPessoaAndInstituicao(Pessoa pessoa, Instituicao instituicao);

    List<PessoaInstituicao> findByPessoa(Pessoa pessoa);

}
