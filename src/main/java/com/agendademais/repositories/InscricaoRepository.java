package com.agendademais.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.agendademais.entities.Inscricao;
import com.agendademais.entities.Instituicao;
import com.agendademais.entities.Pessoa;

public interface InscricaoRepository extends JpaRepository<Inscricao, Long> {

    void deleteByIdPessoaAndIdInstituicao(Pessoa idPessoa, Instituicao idInstituicao);

    void deleteByIdPessoa(Pessoa idPessoa);
    List<Inscricao> findByIdPessoa(Pessoa idPessoa);

    Optional<Inscricao> findByIdPessoaAndIdInstituicao(Pessoa pessoa, Instituicao instituicao);
    
    // VERIFICA INSCRICAO COM DUPLICIDADE DE ATIVIDADE PARA A MESMA PESSOA E INSTITUICAO
    boolean existsByIdPessoaAndIdInstituicaoAndTiposAtividade_Id(Pessoa idPessoa, Instituicao idInstituicao, Long tipoAtividadeId);

}
