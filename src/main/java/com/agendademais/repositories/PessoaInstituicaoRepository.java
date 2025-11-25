package com.agendademais.repositories;

import com.agendademais.entities.Instituicao;
import com.agendademais.entities.Pessoa;
import com.agendademais.entities.PessoaInstituicao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PessoaInstituicaoRepository extends JpaRepository<PessoaInstituicao, Long> {
    long countByInstituicaoId(Long instituicaoId);

    boolean existsByPessoaId(Long pessoaId);

    void deleteAllByPessoaId(Long pessoaId);

    void deleteByPessoaAndInstituicao(Pessoa pessoa, Instituicao instituicao);

    List<PessoaInstituicao> findByPessoa(Pessoa pessoa);

    List<PessoaInstituicao> findByPessoaId(Long pessoaId);

    // Busca PessoaInstituicao por IDs
    Optional<PessoaInstituicao> findByPessoaIdAndInstituicaoId(Long pessoaId, Long instituicaoId);

}
