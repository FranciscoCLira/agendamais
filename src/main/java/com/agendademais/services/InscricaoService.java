package com.agendademais.services;

import com.agendademais.dtos.InscricaoForm;
import com.agendademais.entities.*;
import com.agendademais.exceptions.BusinessException;
import com.agendademais.repositories.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

@Service
public class InscricaoService {
    @Autowired
    private InscricaoRepository inscricaoRepository;
    
    @Autowired
    private TipoAtividadeRepository tipoAtividadeRepository;

  
    public Inscricao findById(Long id) {
        return inscricaoRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Inscrição não encontrada!"));
    }
  
    public void processarInscricao(Pessoa pessoa, Instituicao instituicao, InscricaoForm form) {
        // Busca inscrição existente
        Optional<Inscricao> optInscricao = inscricaoRepository.findByIdPessoaAndIdInstituicao(pessoa, instituicao);
        Inscricao inscricao = optInscricao.orElse(null);

        // 1. Não pode salvar inscrição vazia
        if (form.getTiposAtividadeIds() == null || form.getTiposAtividadeIds().isEmpty()) {
            if (inscricao != null) {
                inscricao.getTiposAtividade().clear();
                inscricaoRepository.save(inscricao);
                inscricaoRepository.delete(inscricao);
            }
            throw new BusinessException("Selecione uma ou mais atividades para se inscrever.");
        }

        // 2. Valida instituição das atividades
        for (Long idTipo : form.getTiposAtividadeIds()) {
            TipoAtividade tipo = tipoAtividadeRepository.findById(idTipo)
                    .orElseThrow(() -> new BusinessException("Tipo de atividade não encontrado."));
            if (!Objects.equals(tipo.getInstituicao().getId(), instituicao.getId())) {
                throw new BusinessException("Uma ou mais atividades não pertencem à sua instituição.");
            }
        }

        // 3. Cria/atualiza inscrição
        if (inscricao == null) {
            inscricao = new Inscricao();
            inscricao.setIdPessoa(pessoa);
            inscricao.setIdInstituicao(instituicao);
            inscricao.setDataInclusao(LocalDate.now());
        }
        inscricao.setDataUltimaAtualizacao(LocalDate.now());
        inscricao.setComentarios(form.getComentarios());

        // 4. Atualiza atividades: remove desmarcadas
        inscricao.getTiposAtividade().removeIf(ita ->
                form.getTiposAtividadeIds().stream()
                        .noneMatch(id -> id.equals(ita.getTipoAtividade().getId()))
        );

        // 5. Adiciona novas marcadas
        for (Long idTipo : form.getTiposAtividadeIds()) {
            boolean jaExiste = inscricao.getTiposAtividade().stream()
                    .anyMatch(ita -> ita.getTipoAtividade().getId().equals(idTipo));
            if (!jaExiste) {
                TipoAtividade tipo = tipoAtividadeRepository.findById(idTipo)
                        .orElseThrow(() -> new BusinessException("Tipo de atividade não encontrado."));
                InscricaoTipoAtividade ita = new InscricaoTipoAtividade();
                ita.setInscricao(inscricao);
                ita.setTipoAtividade(tipo);
                inscricao.getTiposAtividade().add(ita);
            }
        }
        inscricaoRepository.save(inscricao);
    }    
    
}
