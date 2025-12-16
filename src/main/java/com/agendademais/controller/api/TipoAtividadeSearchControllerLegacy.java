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
 * API para busca de Tipos de Atividade por Instituição
 * Utilizada pelo formulário de inscrição massiva para popular o dropdown.
 */
@RestController("tipoAtividadeSearchControllerLegacyBean")
@RequestMapping("/api/tipos-atividade-legacy")
public class TipoAtividadeSearchControllerLegacy {

    @Autowired
    private TipoAtividadeRepository tipoAtividadeRepository;

    /**
     * Retorna lista de tipos de atividade da instituição atual, opcionalmente
     * filtrada por termo.
     * Saída mínima com id e título para uso em dropdown.
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<TipoAtividadeDTO>> buscar(
            @RequestParam(name = "instituicaoId", required = false) Long instituicaoId,
            @RequestParam(name = "term", required = false) String term,
            HttpSession session) {

        // Determina instituição pelo parâmetro ou sessão
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

        List<TipoAtividadeDTO> resultado = tipos.stream()
                .map(t -> new TipoAtividadeDTO(t.getId(), t.getTituloTipoAtividade()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(resultado);
    }

    /**
     * DTO mínimo para resposta de listagem de tipos.
     */
    public static class TipoAtividadeDTO {
        private Long id;
        private String titulo;

        public TipoAtividadeDTO(Long id, String titulo) {
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
