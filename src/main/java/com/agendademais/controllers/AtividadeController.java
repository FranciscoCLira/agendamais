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

    // Nova Atividade (formulário)
    @GetMapping("/novo")
    public String novoAtividade(Model model, HttpSession session) {
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
            model.addAttribute("tiposAtividade", tipoAtividadeRepo.findAll());
            model.addAttribute("subinstituicoes", subInstituicaoRepo.findAll());
        }
        model.addAttribute("pessoas", pessoaRepo.findAll());
        return "atividade-form";
    }

    // Protege rota de exclusão direta: sempre redireciona para /acesso
    @GetMapping("/deletar/{id}")
    public String deletarProtegidoGet(@PathVariable Long id,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("mensagemErro",
                "Exclusão não permitida, pois existem Ocorrências vinculadas à atividade.");
        return "redirect:/administrador/atividades";
    }

    @PostMapping("/deletar/{id}")
    public String deletarProtegidoPost(@PathVariable Long id,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("mensagemErro",
                "Exclusão de atividade só pode ser feita pelo administrador.");
        return "redirect:/administrador/atividades";
    }
    // ...existing code...

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
    public String editar(@PathVariable Long id, Model model, HttpSession session) {
        Atividade atividade = atividadeRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID inválido: " + id));
        model.addAttribute("atividade", atividade);
        com.agendademais.entities.Instituicao instituicaoSelecionada = (com.agendademais.entities.Instituicao) session
                .getAttribute("instituicaoSelecionada");
        if (instituicaoSelecionada != null) {
            model.addAttribute("tiposAtividade", tipoAtividadeRepo.findByInstituicao(instituicaoSelecionada));
            model.addAttribute("subinstituicoes",
                    subInstituicaoRepo.findByInstituicaoAndSituacaoSubInstituicao(instituicaoSelecionada, "A"));
        } else {
            model.addAttribute("tiposAtividade", tipoAtividadeRepo.findAll());
            model.addAttribute("subinstituicoes", subInstituicaoRepo.findAll());
        }
        model.addAttribute("instituicoes", instituicaoRepo.findAll());
        model.addAttribute("pessoas", pessoaRepo.findAll());
        return "atividade-form";
    }

}
