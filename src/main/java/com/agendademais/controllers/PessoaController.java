package com.agendademais.controllers;

import com.agendademais.entities.Pessoa;
import com.agendademais.repositories.PessoaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/pessoas")
public class PessoaController {

    private final PessoaRepository pessoaRepository;

    public PessoaController(PessoaRepository pessoaRepository) {
        this.pessoaRepository = pessoaRepository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pessoas", pessoaRepository.findAll());
        return "pessoas/lista";
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        model.addAttribute("pessoa", new Pessoa());
        return "pessoas/form";
    }

    @PostMapping
    public String salvar(@ModelAttribute Pessoa pessoa) {
        pessoa.setDataInclusao(LocalDate.now());
        pessoa.setDataUltimaAtualizacao(LocalDate.now());
        pessoaRepository.save(pessoa);
        return "redirect:/pessoas";
    }
}
