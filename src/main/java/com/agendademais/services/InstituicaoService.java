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
    private com.agendademais.repositories.UsuarioInstituicaoRepository usuarioInstituicaoRepository;

    @Autowired
    private com.agendademais.repositories.PessoaInstituicaoRepository pessoaInstituicaoRepository;

    public java.util.List<Instituicao> findAll() {
        return instituicaoRepository.findAll();
    }

    @Autowired
    private InstituicaoRepository instituicaoRepository;

    @Autowired
    private CryptoService cryptoService;

    public Optional<Instituicao> findById(Long id) {
        return instituicaoRepository.findById(id);
    }

    public Instituicao save(Instituicao instituicao) {
        // Encrypt smtp password if present and not already encrypted
        if (instituicao.getSmtpPassword() != null && !instituicao.getSmtpPassword().isBlank()) {
            String pwd = instituicao.getSmtpPassword();
            try {
                String encrypted = cryptoService.encryptIfNeeded(pwd);
                instituicao.setSmtpPassword(encrypted);
            } catch (Exception e) {
                // fallback: keep original
                instituicao.setSmtpPassword(pwd);
            }
        }
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
        Optional<Instituicao> opt = instituicaoRepository.findById(id);
        if (opt.isEmpty())
            return false;
        Instituicao inst = opt.get();
        // Only allow delete if status is Inativa and no user/person links
        if (!"I".equalsIgnoreCase(inst.getSituacaoInstituicao())) {
            return false;
        }
        long usuarioLinks = usuarioInstituicaoRepository.countByInstituicaoId(id);
        long pessoaLinks = pessoaInstituicaoRepository.countByInstituicaoId(id);
        if (usuarioLinks > 0 || pessoaLinks > 0) {
            return false;
        }
        instituicaoRepository.deleteById(id);
        return true;
    }
}
