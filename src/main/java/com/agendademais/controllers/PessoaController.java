package com.agendademais.controllers;

import org.springframework.web.bind.annotation.ResponseBody;
import java.util.List;
import java.util.stream.Collectors;
import com.agendademais.dto.PessoaAutocompleteDTO;
import org.springframework.web.bind.annotation.RequestParam;

import com.agendademais.entities.Pessoa;
import com.agendademais.repositories.PessoaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
public class PessoaController {

    private final PessoaRepository pessoaRepository;

    public PessoaController(PessoaRepository pessoaRepository) {
        this.pessoaRepository = pessoaRepository;
    }

    // Endpoint para autocomplete de pessoas (global)
    @GetMapping("/api/pessoas/autocomplete")
    @ResponseBody
    public List<PessoaAutocompleteDTO> autocompletePessoas(@RequestParam("q") String query) {
        List<Pessoa> pessoas = pessoaRepository
                .findTop10ByNomePessoaContainingIgnoreCaseOrEmailPessoaContainingIgnoreCase(query, query);
        return pessoas.stream()
                .map(p -> new PessoaAutocompleteDTO(p.getId(), p.getNomePessoa(), p.getEmailPessoa()))
                .collect(Collectors.toList());
    }

    @GetMapping("/pessoas")
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
