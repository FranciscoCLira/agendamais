package com.agendademais.controllers;

import com.agendademais.entities.TipoAtividade;
import com.agendademais.repositories.TipoAtividadeRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/tipos-atividade")
public class TipoAtividadeController {

    private final TipoAtividadeRepository tipoAtividadeRepository;

    public TipoAtividadeController(TipoAtividadeRepository tipoAtividadeRepository) {
        this.tipoAtividadeRepository = tipoAtividadeRepository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("tipos", tipoAtividadeRepository.findAll());
        return "tipoatividade/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("tipoAtividade", new TipoAtividade());
        return "tipoatividade/form";
    }

    @PostMapping
    public String salvar(@ModelAttribute TipoAtividade tipoAtividade) {
        tipoAtividadeRepository.save(tipoAtividade);
        return "redirect:/tipos-atividade";
    }
}
