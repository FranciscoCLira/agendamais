package com.agendademais.repositories;

import com.agendademais.entities.Atividade;
import com.agendademais.entities.OcorrenciaAtividade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OcorrenciaAtividadeRepository extends JpaRepository<OcorrenciaAtividade, Long> {
    Page<OcorrenciaAtividade> findByIdAtividade(Atividade idAtividade, Pageable pageable);
}
