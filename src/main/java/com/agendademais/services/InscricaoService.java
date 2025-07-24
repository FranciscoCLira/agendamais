package com.agendademais.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.agendademais.entities.*;
import com.agendademais.repositories.*;

import com.agendademais.exceptions.BusinessException;

@Service
public class InscricaoService {
    @Autowired
    private InscricaoRepository inscricaoRepository;

    public Inscricao findById(Long id) {
        return inscricaoRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Inscrição não encontrada!"));
    }
 
}
