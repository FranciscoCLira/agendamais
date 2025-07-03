package com.agendademais.controllers;

import com.agendademais.entities.*;
import com.agendademais.repositories.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/pessoa-subinstituicao")
public class PessoaSubInstituicaoController {

    private final PessoaSubInstituicaoRepository pessoaSubInstituicaoRepository;
    private final PessoaRepository pessoaRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final SubInstituicaoRepository subInstituicaoRepository;

    public PessoaSubInstituicaoController(PessoaSubInstituicaoRepository pessoaSubInstituicaoRepository,
                                          PessoaRepository pessoaRepository,
                                          InstituicaoRepository instituicaoRepository,
                                          SubInstituicaoRepository subInstituicaoRepository) {
        this.pessoaSubInstituicaoRepository = pessoaSubInstituicaoRepository;
        this.pessoaRepository = pessoaRepository;
        this.instituicaoRepository = instituicaoRepository;
        this.subInstituicaoRepository = subInstituicaoRepository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("vinculos", pessoaSubInstituicaoRepository.findAll());
        return "pessoa-subinstituicao-list";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("pessoas", pessoaRepository.findAll());
        model.addAttribute("instituicoes", instituicaoRepository.findAll());
        model.addAttribute("subinstituicoes", subInstituicaoRepository.findAll());
        return "pessoa-subinstituicao-form";
    }

    @PostMapping("/salvar")
    public String salvar(@RequestParam Long pessoaId,
                         @RequestParam Long instituicaoId,
                         @RequestParam Long subInstituicaoId,
                         @RequestParam(required = false) String identificacao,
                         @RequestParam(required = false) String dataAfiliacao) {

        PessoaSubInstituicao psi = new PessoaSubInstituicao();
        psi.setPessoa(pessoaRepository.findById(pessoaId).orElse(null));
        psi.setInstituicao(instituicaoRepository.findById(instituicaoId).orElse(null));
        psi.setSubInstituicao(subInstituicaoRepository.findById(subInstituicaoId).orElse(null));
        psi.setIdentificacaoPessoaSubInstituicao(identificacao);
        if (dataAfiliacao != null && !dataAfiliacao.isEmpty()) {
            psi.setDataAfiliacao(LocalDate.parse(dataAfiliacao));
        }
        psi.setDataUltimaAtualizacao(LocalDate.now());

        pessoaSubInstituicaoRepository.save(psi);
        return "redirect:/pessoa-subinstituicao";
    }

    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id) {
        pessoaSubInstituicaoRepository.deleteById(id);
        return "redirect:/pessoa-subinstituicao";
    }
}

