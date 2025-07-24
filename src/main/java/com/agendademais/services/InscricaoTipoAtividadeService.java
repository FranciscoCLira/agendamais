package com.agendademais.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.agendademais.entities.*;
import com.agendademais.exceptions.BusinessException;
import com.agendademais.repositories.*;

@Service
public class InscricaoTipoAtividadeService {
    @Autowired
    private InscricaoTipoAtividadeRepository repository;

    public InscricaoTipoAtividade vincularTipoAtividade(Inscricao inscricao, TipoAtividade tipoAtividade) {

    	Optional<InscricaoTipoAtividade> existente = repository.findByInscricaoAndTipoAtividade(inscricao, tipoAtividade);

    	if (existente.isPresent()) {
            throw new BusinessException("Tipo de atividade já vinculado nesta inscrição.");
        }
        InscricaoTipoAtividade nova = new InscricaoTipoAtividade(inscricao, tipoAtividade);
        return repository.save(nova);
    }
}

