package com.agendademais.controllers;

import com.agendademais.entities.Atividade;
import com.agendademais.entities.OcorrenciaAtividade;
import com.agendademais.repositories.AtividadeRepository;
import com.agendademais.repositories.OcorrenciaAtividadeRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.Optional;

@Controller
@RequestMapping("/administrador/ocorrencias")
public class AdministradorOcorrenciasController {
    @Autowired
    private OcorrenciaAtividadeRepository ocorrenciaAtividadeRepository;
    @Autowired
    private AtividadeRepository atividadeRepository;

    @GetMapping
    public String listarOcorrencias(
            @RequestParam(value = "atividadeId", required = false) Long atividadeId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "25") int size,
            Model model,
            HttpSession session
    ) {
        // Se veio do menu, sem atividade selecionada
        if (atividadeId == null) {
            model.addAttribute("ocorrencias", Collections.emptyList());
            model.addAttribute("atividadeSelecionada", null);
            return "administrador/ocorrencias";
        }
        // Se veio da tela de atividades
        Optional<Atividade> atividadeOpt = atividadeRepository.findById(atividadeId);
        if (atividadeOpt.isEmpty()) {
            model.addAttribute("ocorrencias", Collections.emptyList());
            model.addAttribute("atividadeSelecionada", null);
            return "administrador/ocorrencias";
        }
        Atividade atividade = atividadeOpt.get();
        Pageable pageable = PageRequest.of(page, size);
        Page<OcorrenciaAtividade> ocorrencias = ocorrenciaAtividadeRepository.findByIdAtividade(atividade, pageable);
        model.addAttribute("atividadeSelecionada", atividade);
        model.addAttribute("ocorrencias", ocorrencias);
        // Se não houver ocorrências, redireciona para nova ocorrência
        if (ocorrencias.isEmpty()) {
            return "redirect:/administrador/ocorrencias/nova?atividadeId=" + atividade.getId();
        }
        return "administrador/ocorrencias";
    }

    @GetMapping("/nova")
    public String novaOcorrencia(@RequestParam(value = "atividadeId", required = false) Long atividadeId, Model model) {
        OcorrenciaAtividade ocorrencia = new OcorrenciaAtividade();
        if (atividadeId != null) {
            atividadeRepository.findById(atividadeId).ifPresent(ocorrencia::setIdAtividade);
        }
        model.addAttribute("ocorrencia", ocorrencia);
        return "administrador/ocorrencia-form";
    }
}
