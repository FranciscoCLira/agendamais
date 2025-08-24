package com.agendademais.api;

import com.agendademais.entities.TipoAtividade;
import com.agendademais.repositories.TipoAtividadeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import jakarta.servlet.http.HttpSession;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tipos-atividade")
public class TipoAtividadeApiController {

    @Autowired
    private TipoAtividadeRepository tipoAtividadeRepository;

    @GetMapping
    public List<TipoAtividade> listarTodos(HttpSession session) {
        com.agendademais.entities.Instituicao instituicao = (com.agendademais.entities.Instituicao) session
                .getAttribute("instituicaoSelecionada");
        if (instituicao == null) {
            return java.util.Collections.emptyList();
        }
        return tipoAtividadeRepository.findByInstituicao(instituicao);
    }

    @PostMapping
    public TipoAtividade criar(@RequestBody TipoAtividade tipoAtividade, HttpSession session) {
        com.agendademais.entities.Instituicao instituicao = (com.agendademais.entities.Instituicao) session
                .getAttribute("instituicaoSelecionada");
        if (instituicao != null) {
            tipoAtividade.setInstituicao(instituicao);
        }
        return tipoAtividadeRepository.save(tipoAtividade);
    }

    @GetMapping("/{id}")
    public TipoAtividade buscarPorId(@PathVariable Long id, HttpSession session) {
        var tipoOpt = tipoAtividadeRepository.findById(id);
        if (tipoOpt.isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Tipo de Atividade não encontrado");
        }
        com.agendademais.entities.Instituicao instituicao = (com.agendademais.entities.Instituicao) session
                .getAttribute("instituicaoSelecionada");
        TipoAtividade tipo = tipoOpt.get();
        if (instituicao != null && tipo.getInstituicao().getId().equals(instituicao.getId())) {
            return tipo;
        } else {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Não autorizado");
        }
    }

    @PutMapping("/{id}")
    public TipoAtividade atualizar(@PathVariable Long id, @RequestBody TipoAtividade tipoAtividade,
            HttpSession session) {
        var existente = tipoAtividadeRepository.findById(id).orElse(null);
        if (existente == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Tipo de Atividade não encontrado");
        }
        tipoAtividade.setId(id);
        com.agendademais.entities.Instituicao instituicao = (com.agendademais.entities.Instituicao) session
                .getAttribute("instituicaoSelecionada");
        if (instituicao != null) {
            tipoAtividade.setInstituicao(instituicao);
        }
        return tipoAtividadeRepository.save(tipoAtividade);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id, HttpSession session) {
        com.agendademais.entities.Instituicao instituicao = (com.agendademais.entities.Instituicao) session
                .getAttribute("instituicaoSelecionada");
        var tipoOpt = tipoAtividadeRepository.findById(id);
        if (tipoOpt.isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Tipo de Atividade não encontrado");
        }
        TipoAtividade tipo = tipoOpt.get();
        if (instituicao != null && tipo.getInstituicao().getId().equals(instituicao.getId())) {
            tipoAtividadeRepository.deleteById(id);
        } else {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Não autorizado");
        }
    }
}
