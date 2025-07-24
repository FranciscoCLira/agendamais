package com.agendademais.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.agendademais.entities.*;
import com.agendademais.repositories.*;

import com.agendademais.exceptions.BusinessException;

@Service
public class TipoAtividadeService {
    @Autowired
    private TipoAtividadeRepository tipoAtividadeRepository;

    public TipoAtividade findById(Long id) {
        return tipoAtividadeRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Tipo de atividade não encontrado!"));
    }
}