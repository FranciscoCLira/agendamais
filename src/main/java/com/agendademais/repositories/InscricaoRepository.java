package com.agendademais.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.agendademais.entities.Inscricao;
import com.agendademais.entities.Instituicao;
import com.agendademais.entities.Pessoa;

public interface InscricaoRepository extends JpaRepository<Inscricao, Long> {

    void deleteByPessoaAndIdInstituicao(Pessoa pessoa, Instituicao idInstituicao);

    void deleteByPessoa(Pessoa pessoa);

    List<Inscricao> findByPessoa(Pessoa pessoa);

    Optional<Inscricao> findByPessoaAndIdInstituicao(Pessoa pessoa, Instituicao instituicao);

    // VERIFICA INSCRICAO COM DUPLICIDADE DE ATIVIDADE PARA A MESMA PESSOA E
    // INSTITUICAO
    boolean existsByPessoaAndIdInstituicaoAndTiposAtividade_Id(Pessoa pessoa, Instituicao idInstituicao,
            Long tipoAtividadeId);

}
