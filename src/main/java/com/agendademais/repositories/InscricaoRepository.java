package com.agendademais.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.agendademais.entities.Inscricao;
import com.agendademais.entities.Instituicao;
import com.agendademais.entities.Pessoa;

public interface InscricaoRepository extends JpaRepository<Inscricao, Long> {

    void deleteByIdPessoaAndIdInstituicao(Pessoa idPessoa, Instituicao idInstituicao);

    void deleteByIdPessoa(Pessoa idPessoa);
    List<Inscricao> findByIdPessoa(Pessoa idPessoa);
    
    List<Inscricao> findByIdPessoaAndIdInstituicao(Pessoa idPessoa, Instituicao idInstituicao);
}
