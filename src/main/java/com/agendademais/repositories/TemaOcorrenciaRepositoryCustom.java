package com.agendademais.repositories;

import java.util.List;
import org.springframework.data.repository.query.Param;

public interface TemaOcorrenciaRepositoryCustom {
    List<String> findDistinctTemaOcorrenciaByTerm(@Param("term") String term);
}
