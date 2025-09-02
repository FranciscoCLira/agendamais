package com.agendademais.repositories;

import java.util.List;

public interface TemaOcorrenciaRepository {
    List<String> findDistinctTemaOcorrenciaByTerm(String term);
}
