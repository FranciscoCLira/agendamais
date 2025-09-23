package com.agendademais.controllers;

import com.agendademais.entities.TipoAtividade;
import com.agendademais.repositories.TipoAtividadeRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/administrador/tipos-atividade")
public class AdministradorTipoAtividadeController {

    private final TipoAtividadeRepository tipoAtividadeRepository;

    public AdministradorTipoAtividadeController(TipoAtividadeRepository tipoAtividadeRepository) {
        this.tipoAtividadeRepository = tipoAtividadeRepository;
    }

    @GetMapping
    public String listar(Model model, HttpSession session) {
        var instituicao = (com.agendademais.entities.Instituicao) session.getAttribute("instituicaoSelecionada");
        if (instituicao == null) {
            model.addAttribute("tiposAtividade", java.util.Collections.emptyList());
        } else {
            model.addAttribute("tiposAtividade", tipoAtividadeRepository.findByInstituicao(instituicao));
        }
        return "administrador/tipos-atividade";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("tipoAtividade", new TipoAtividade());
        return "administrador/tipo-atividade-form";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        TipoAtividade tipo = tipoAtividadeRepository.findById(id).orElseThrow();
        model.addAttribute("tipoAtividade", tipo);
        return "administrador/tipo-atividade-form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute TipoAtividade tipoAtividade, HttpSession session) {
        var instituicao = (com.agendademais.entities.Instituicao) session.getAttribute("instituicaoSelecionada");
        if (instituicao != null) {
            tipoAtividade.setInstituicao(instituicao);
        }
        tipoAtividadeRepository.save(tipoAtividade);
        return "redirect:/administrador/tipos-atividade";
    }

    @GetMapping("/deletar/{id}")
    public String deletar(@PathVariable Long id) {
        tipoAtividadeRepository.deleteById(id);
        return "redirect:/administrador/tipos-atividade";
    }
}
