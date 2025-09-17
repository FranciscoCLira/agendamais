package com.agendademais.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.agendademais.entities.*;

public interface InscricaoTipoAtividadeRepository extends JpaRepository<InscricaoTipoAtividade, Long> {

    Optional<InscricaoTipoAtividade> findByInscricaoAndTipoAtividade(Inscricao inscricao, TipoAtividade tipoAtividade);

    Optional<InscricaoTipoAtividade> findByInscricaoIdAndTipoAtividadeId(Long inscricaoId, Long tipoAtividadeId);

    List<InscricaoTipoAtividade> findByInscricao(Inscricao inscricao);

    // Conta quantas inscrições existem para um tipo de atividade e instituição
    long countByTipoAtividadeIdAndInscricao_IdInstituicao_Id(Long tipoAtividadeId, Long instituicaoId);
}
