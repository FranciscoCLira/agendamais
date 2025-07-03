package com.agendademais.controllers;

import com.agendademais.entities.*;
import com.agendademais.repositories.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/pessoa-instituicao")
public class PessoaInstituicaoController {

    private final PessoaInstituicaoRepository pessoaInstituicaoRepository;
    private final PessoaRepository pessoaRepository;
    private final InstituicaoRepository instituicaoRepository;

    public PessoaInstituicaoController(PessoaInstituicaoRepository pessoaInstituicaoRepository,
                                       PessoaRepository pessoaRepository,
                                       InstituicaoRepository instituicaoRepository) {
        this.pessoaInstituicaoRepository = pessoaInstituicaoRepository;
        this.pessoaRepository = pessoaRepository;
        this.instituicaoRepository = instituicaoRepository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("vinculos", pessoaInstituicaoRepository.findAll());
        return "pessoa-instituicao-list";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("pessoas", pessoaRepository.findAll());
        model.addAttribute("instituicoes", instituicaoRepository.findAll());
        return "pessoa-instituicao-form";
    }

    @PostMapping("/salvar")
    public String salvar(@RequestParam Long pessoaId,
                         @RequestParam Long instituicaoId,
                         @RequestParam(required = false) String identificacao,
                         @RequestParam(required = false) String dataAfiliacao) {

        PessoaInstituicao pi = new PessoaInstituicao();
        pi.setPessoa(pessoaRepository.findById(pessoaId).orElse(null));
        pi.setInstituicao(instituicaoRepository.findById(instituicaoId).orElse(null));
        pi.setIdentificacaoPessoaInstituicao(identificacao);
        if (dataAfiliacao != null && !dataAfiliacao.isEmpty()) {
            pi.setDataAfiliacao(LocalDate.parse(dataAfiliacao));
        }
        pi.setDataUltimaAtualizacao(LocalDate.now());

        pessoaInstituicaoRepository.save(pi);
        return "redirect:/pessoa-instituicao";
    }

    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id) {
        pessoaInstituicaoRepository.deleteById(id);
        return "redirect:/pessoa-instituicao";
    }
}
