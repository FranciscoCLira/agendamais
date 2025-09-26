package com.agendademais.services;

import com.agendademais.entities.Instituicao;
import com.agendademais.repositories.InstituicaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class InstituicaoService {
    @Autowired
    private InstituicaoRepository instituicaoRepository;

    public Optional<Instituicao> findById(Long id) {
        return instituicaoRepository.findById(id);
    }

    public Instituicao save(Instituicao instituicao) {
        return instituicaoRepository.save(instituicao);
    }

    public void updateEmail(Long id, String email) {
        instituicaoRepository.findById(id).ifPresent(inst -> {
            inst.setEmailInstituicao(email);
            instituicaoRepository.save(inst);
        });
    }

    @Transactional
    public boolean deleteIfNoRelations(Long id) {
        // Implement relation checks as needed
        instituicaoRepository.deleteById(id);
        return true;
    }
}
