package com.agendademais.controller.api;

import java.util.List;
import java.util.stream.Collectors;

import com.agendademais.entities.Instituicao;
import com.agendademais.entities.TipoAtividade;
import com.agendademais.repositories.TipoAtividadeRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * API dedicada à busca de Tipos de Atividade para preenchimento de dropdowns.
 * Mantém a rota esperada pelo front: /api/tipos-atividade/buscar
 */
@RestController("tipoAtividadeSearchControllerMain")
@RequestMapping("/api/tipos-atividade-main")
public class TipoAtividadeSearchController {

    @Autowired
    private TipoAtividadeRepository tipoAtividadeRepository;

    @GetMapping("/buscar")
    public ResponseEntity<List<TipoItemDTO>> buscar(
            @RequestParam(name = "instituicaoId", required = false) Long instituicaoId,
            @RequestParam(name = "term", required = false) String term,
            HttpSession session) {

        Long alvoInstituicaoId = instituicaoId;
        if (alvoInstituicaoId == null) {
            Instituicao instituicaoSelecionada = (Instituicao) session.getAttribute("instituicaoSelecionada");
            if (instituicaoSelecionada != null) {
                alvoInstituicaoId = instituicaoSelecionada.getId();
            }
        }

        if (alvoInstituicaoId == null) {
            return ResponseEntity.badRequest().build();
        }

        List<TipoAtividade> tipos;
        if (term != null && !term.isBlank()) {
            tipos = tipoAtividadeRepository
                    .findByInstituicaoIdAndTituloTipoAtividadeContainingIgnoreCase(alvoInstituicaoId, term.trim());
        } else {
            tipos = tipoAtividadeRepository.findByInstituicaoId(alvoInstituicaoId);
        }

        List<TipoItemDTO> resultado = tipos.stream()
                .map(t -> new TipoItemDTO(t.getId(), t.getTituloTipoAtividade()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(resultado);
    }

    public static class TipoItemDTO {
        private Long id;
        private String titulo;

        public TipoItemDTO(Long id, String titulo) {
            this.id = id;
            this.titulo = titulo;
        }

        public Long getId() {
            return id;
        }

        public String getTitulo() {
            return titulo;
        }
    }
}
