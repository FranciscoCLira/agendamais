package com.agendademais.api;

import com.agendademais.entities.SubInstituicao;
import com.agendademais.entities.TipoAtividade;
import com.agendademais.repositories.SubInstituicaoRepository;
import com.agendademais.repositories.TipoAtividadeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * API REST para autocomplete de SubInstituições e Tipos de Atividade
 */
@RestController
@RequestMapping("/api")
public class AutocompleteApiController {

    @Autowired
    private SubInstituicaoRepository subInstituicaoRepository;

    @Autowired
    private TipoAtividadeRepository tipoAtividadeRepository;

    /**
     * Busca SubInstituições por termo e instituição
     */
    @GetMapping("/subinstituicoes/buscar")
    public ResponseEntity<List<Map<String, Object>>> buscarSubInstituicoes(
            @RequestParam(required = false) String termo,
            @RequestParam Long instituicaoId) {

        List<SubInstituicao> subInstituicoes;

        if (termo != null && !termo.trim().isEmpty()) {
            subInstituicoes = subInstituicaoRepository.findByInstituicaoIdAndNomeSubInstituicaoContainingIgnoreCase(
                    instituicaoId, termo);
        } else {
            subInstituicoes = subInstituicaoRepository.findByInstituicaoId(instituicaoId);
        }

        List<Map<String, Object>> result = subInstituicoes.stream()
                .map(sub -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", sub.getId());
                    map.put("nomeSubInstituicao", sub.getNomeSubInstituicao());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * Busca Tipos de Atividade por termo e instituição
     */
    @GetMapping("/tipos-atividade/buscar")
    public ResponseEntity<List<Map<String, Object>>> buscarTiposAtividade(
            @RequestParam(required = false) String termo,
            @RequestParam Long instituicaoId) {

        List<TipoAtividade> tiposAtividade;

        if (termo != null && !termo.trim().isEmpty()) {
            tiposAtividade = tipoAtividadeRepository.findByInstituicaoIdAndTituloTipoAtividadeContainingIgnoreCase(
                    instituicaoId, termo);
        } else {
            tiposAtividade = tipoAtividadeRepository.findByInstituicaoId(instituicaoId);
        }

        List<Map<String, Object>> result = tiposAtividade.stream()
                .map(tipo -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", tipo.getId());
                    map.put("tituloTipoAtividade", tipo.getTituloTipoAtividade());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}
