package com.agendademais.controllers;

import com.agendademais.entities.Atividade;
import com.agendademais.entities.Pessoa;
import com.agendademais.repositories.*;

import java.time.LocalDate;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/atividades")
public class AtividadeController {

    @Autowired
    private TipoAtividadeRepository tipoAtividadeRepo;
    @Autowired
    private InstituicaoRepository instituicaoRepo;
    @Autowired
    private SubInstituicaoRepository subInstituicaoRepo;
    @Autowired
    private AtividadeRepository atividadeRepo;
    @Autowired
    private PessoaRepository pessoaRepo;

    

    @GetMapping("/atividade-form")
    public String exibirFormularioAtividade(HttpSession session, Model model) {
        String nomeInstituicao = (String) session.getAttribute("nomeInstituicao");
        model.addAttribute("nomeInstituicao", nomeInstituicao);
        return "atividade-form";
    }

    @GetMapping("/atividade-lista")
    public String listarAtividades(HttpSession session, Model model) {
        String nomeInstituicao = (String) session.getAttribute("nomeInstituicao");
        model.addAttribute("nomeInstituicao", nomeInstituicao);
        return "atividade-lista";
    }    
    
    
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("atividades", atividadeRepo.findAll());
        
        // PRINTS NA CONSOLE PARA TESTES 
        System.out.println(" ### listar: atividades ****************");
        System.out.println("   Tipos: " + tipoAtividadeRepo.count());
        System.out.println("   Instituições: " + instituicaoRepo.count());
        System.out.println("   Subinstituições: " + subInstituicaoRepo.count());
        
        return "atividade-lista";
    }

    @GetMapping("/novo")
    public String novaAtividade(Model model) {
        model.addAttribute("atividade", new Atividade());
        model.addAttribute("tiposAtividade", tipoAtividadeRepo.findAll());
        model.addAttribute("instituicoes", instituicaoRepo.findAll());
        model.addAttribute("subinstituicoes", subInstituicaoRepo.findAll());
        model.addAttribute("pessoas", pessoaRepo.findAll());
        
        // PRINTS NA CONSOLE PARA TESTES 
        System.out.println(" ### /Novo: Carregando nova atividade ********");
        System.out.println("   Tipos: " + tipoAtividadeRepo.count());
        System.out.println("   Instituições: " + instituicaoRepo.count());
        System.out.println("   Subinstituições: " + subInstituicaoRepo.count());

        return "atividade-form";
    }

    @PostMapping("/salvar")
    public String salvarAtividade(@ModelAttribute Atividade atividade,
                                  @RequestParam("emailSolicitante") String emailSolicitante) {

        Pessoa solicitante = pessoaRepo.findByEmailPessoa(emailSolicitante)
                .orElseThrow(() -> new IllegalArgumentException("Pessoa com e-mail '" + emailSolicitante + "' não encontrada."));
    	
        atividade.setIdSolicitante(solicitante);
        atividade.setDataAtualizacao(LocalDate.now());

        atividadeRepo.save(atividade);
        return "redirect:/atividades";
    }

    
    
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Atividade atividade = atividadeRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("ID inválido: " + id));
        model.addAttribute("atividade", atividade);
        model.addAttribute("tiposAtividade", tipoAtividadeRepo.findAll());
        model.addAttribute("instituicoes", instituicaoRepo.findAll());
        model.addAttribute("subinstituicoes", subInstituicaoRepo.findAll());
        model.addAttribute("pessoas", pessoaRepo.findAll());
        return "atividade-form";
    }

    @GetMapping("/deletar/{id}")
    public String deletar(@PathVariable Long id) {
        atividadeRepo.deleteById(id);
        return "redirect:/atividades";
    }
}
