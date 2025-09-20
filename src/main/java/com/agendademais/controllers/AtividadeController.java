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
    public String novaAtividade(Model model, HttpSession session) {
        if (session.getAttribute("usuarioLogado") == null) {
            return "redirect:/login";
        }
        model.addAttribute("atividade", new Atividade());
        com.agendademais.entities.Instituicao instituicaoSelecionada = (com.agendademais.entities.Instituicao) session
                .getAttribute("instituicaoSelecionada");
        if (instituicaoSelecionada != null) {
            model.addAttribute("tiposAtividade", tipoAtividadeRepo.findByInstituicao(instituicaoSelecionada));
            model.addAttribute("subinstituicoes",
                    subInstituicaoRepo.findByInstituicaoAndSituacaoSubInstituicao(instituicaoSelecionada, "A"));
        } else {
            model.addAttribute("tiposAtividade", java.util.Collections.emptyList());
            model.addAttribute("subinstituicoes", subInstituicaoRepo.findAll());
        }
        model.addAttribute("pessoas", pessoaRepo.findAll());
        return "atividade-form";
    }

    @PostMapping("/salvar")
    public String salvarAtividade(@ModelAttribute Atividade atividade,
            @RequestParam(value = "idSolicitante", required = false) Long idSolicitante,
            HttpSession session,
            Model model) {
        if (session.getAttribute("usuarioLogado") == null) {
            return "redirect:/login";
        }
        if (idSolicitante == null) {
            model.addAttribute("atividade", atividade);
            model.addAttribute("tiposAtividade", tipoAtividadeRepo.findAll());
            com.agendademais.entities.Instituicao instituicaoSelecionada = (com.agendademais.entities.Instituicao) session
                    .getAttribute("instituicaoSelecionada");
            if (instituicaoSelecionada != null) {
                model.addAttribute("subinstituicoes",
                        subInstituicaoRepo.findByInstituicaoAndSituacaoSubInstituicao(instituicaoSelecionada, "A"));
            } else {
                model.addAttribute("subinstituicoes", subInstituicaoRepo.findAll());
            }
            model.addAttribute("pessoas", pessoaRepo.findAll());
            model.addAttribute("erroSolicitante", "Selecione um solicitante antes de salvar.");
            return "atividade-form";
        }
        Pessoa solicitante = pessoaRepo.findById(idSolicitante)
                .orElseThrow(
                        () -> new IllegalArgumentException("Pessoa com id '" + idSolicitante + "' não encontrada."));
        atividade.setIdSolicitante(solicitante);
        atividade.setDataAtualizacao(LocalDate.now());
        // Buscar instituição logada na sessão
        com.agendademais.entities.Instituicao instituicaoSelecionada = (com.agendademais.entities.Instituicao) session
                .getAttribute("instituicaoSelecionada");
        if (instituicaoSelecionada == null) {
            throw new IllegalStateException(
                    "Instituição logada não encontrada na sessão. Faça login pelo menu principal.");
        }
        atividade.setInstituicao(instituicaoSelecionada);
        // Campos opcionais: garantir null se vazio
        if (atividade.getDescricaoAtividade() != null && atividade.getDescricaoAtividade().trim().isEmpty()) {
            atividade.setDescricaoAtividade(null);
        }
        if (atividade.getComentariosAtividade() != null && atividade.getComentariosAtividade().trim().isEmpty()) {
            atividade.setComentariosAtividade(null);
        }
        if (atividade.getLinkMaterialAtividade() != null && atividade.getLinkMaterialAtividade().trim().isEmpty()) {
            atividade.setLinkMaterialAtividade(null);
        }
        if (atividade.getLinkAtividadeOnLine() != null && atividade.getLinkAtividadeOnLine().trim().isEmpty()) {
            atividade.setLinkAtividadeOnLine(null);
        }
        atividadeRepo.save(atividade);
        return "redirect:/administrador/atividades";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Atividade atividade = atividadeRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID inválido: " + id));
        model.addAttribute("atividade", atividade);
        model.addAttribute("tiposAtividade", tipoAtividadeRepo.findAll());
        model.addAttribute("instituicoes", instituicaoRepo.findAll());
        model.addAttribute("subinstituicoes", subInstituicaoRepo.findAll());
        model.addAttribute("pessoas", pessoaRepo.findAll());
        return "atividade-form";
    }

    @GetMapping("/deletar/{id}")
    public String deletar(@PathVariable Long id,
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String situacao,
            @RequestParam(required = false) String forma,
            @RequestParam(required = false) String alvo,
            @RequestParam(required = false) Long subInstituicao,
            @RequestParam(required = false) Long solicitante,
            @RequestParam(required = false) String dataInicio,
            @RequestParam(required = false) String dataFim,
            @RequestParam(required = false) String ordenacao,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        atividadeRepo.deleteById(id);
        // Montar query string para manter filtros e paginação
        StringBuilder qs = new StringBuilder();
        if (titulo != null)
            qs.append("&titulo=").append(titulo);
        if (situacao != null)
            qs.append("&situacao=").append(situacao);
        if (forma != null)
            qs.append("&forma=").append(forma);
        if (alvo != null)
            qs.append("&alvo=").append(alvo);
        if (subInstituicao != null)
            qs.append("&subInstituicao=").append(subInstituicao);
        if (solicitante != null)
            qs.append("&solicitante=").append(solicitante);
        if (dataInicio != null)
            qs.append("&dataInicio=").append(dataInicio);
        if (dataFim != null)
            qs.append("&dataFim=").append(dataFim);
        if (ordenacao != null)
            qs.append("&ordenacao=").append(ordenacao);
        qs.append("&page=").append(page);
        qs.append("&size=").append(size);
        String redirectUrl = "redirect:/administrador/atividades?" + qs.substring(1); // remove primeiro &
        return redirectUrl;
    }
}
