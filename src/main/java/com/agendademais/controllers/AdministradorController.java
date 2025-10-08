package com.agendademais.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.beans.factory.annotation.Autowired;
import com.agendademais.repositories.SubInstituicaoRepository;
import com.agendademais.entities.Instituicao;
import com.agendademais.entities.SubInstituicao;
import java.util.List;

@Controller
public class AdministradorController {

    @Autowired
    private SubInstituicaoRepository subInstituicaoRepository;

    @GetMapping("/administrador")
    public String exibirPainelAdministrador(HttpSession session, Model model) {
        if (session.getAttribute("usuarioLogado") == null) {
            model.addAttribute("mensagemErro", "Sessão expirada. Faça login novamente.");
            return "login";
        }
        String nomeInstituicao = (String) session.getAttribute("nomeInstituicao");
        model.addAttribute("nomeInstituicao", nomeInstituicao);
        return "menus/menu-administrador";
    }

    @GetMapping("/administrador/subinstituicoes")
    public String gerenciarSubInstituicoes(HttpSession session, Model model) {
        if (session.getAttribute("usuarioLogado") == null) {
            model.addAttribute("mensagemErro", "Sessão expirada. Faça login novamente.");
            return "login";
        }
        Instituicao instituicaoSelecionada = (Instituicao) session.getAttribute("instituicaoSelecionada");
        List<SubInstituicao> subinstituicoes = null;
        if (instituicaoSelecionada != null) {
            subinstituicoes = subInstituicaoRepository.findByInstituicao(instituicaoSelecionada);
        }
        model.addAttribute("subinstituicoes", subinstituicoes);
        model.addAttribute("instituicaoSelecionada", instituicaoSelecionada);
        model.addAttribute("subInstituicao", new SubInstituicao());
        return "administrador/gerenciar-subinstituicoes";
    }

    @org.springframework.web.bind.annotation.PostMapping("/administrador/subinstituicoes/nova")
    public String salvarNovaSubInstituicao(
            @org.springframework.web.bind.annotation.ModelAttribute SubInstituicao subInstituicao,
            HttpSession session,
            Model model) {
        Instituicao instituicaoSelecionada = (Instituicao) session.getAttribute("instituicaoSelecionada");
        if (instituicaoSelecionada == null) {
            model.addAttribute("mensagemErro", "Instituição não selecionada.");
            return "redirect:/administrador/subinstituicoes";
        }
        subInstituicao.setInstituicao(instituicaoSelecionada);
        subInstituicao.setDataUltimaAtualizacao(java.time.LocalDate.now());
        subInstituicaoRepository.save(subInstituicao);
        return "redirect:/administrador/subinstituicoes";
    }

    @org.springframework.web.bind.annotation.GetMapping("/administrador/subinstituicoes/editar/{id}")
    public String editarSubInstituicao(@org.springframework.web.bind.annotation.PathVariable Long id, Model model,
            HttpSession session) {
        SubInstituicao sub = subInstituicaoRepository.findById(id).orElse(null);
        if (sub == null) {
            model.addAttribute("mensagemErro", "Sub-Instituição não encontrada.");
            return "redirect:/administrador/subinstituicoes";
        }
        Instituicao instituicaoSelecionada = (Instituicao) session.getAttribute("instituicaoSelecionada");
        List<SubInstituicao> subinstituicoes = null;
        if (instituicaoSelecionada != null) {
            subinstituicoes = subInstituicaoRepository.findByInstituicao(instituicaoSelecionada);
        }
        model.addAttribute("subinstituicoes", subinstituicoes);
        model.addAttribute("instituicaoSelecionada", instituicaoSelecionada);
        model.addAttribute("subInstituicao", sub);
        return "administrador/gerenciar-subinstituicoes";
    }

    @org.springframework.web.bind.annotation.PostMapping("/administrador/subinstituicoes/editar")
    public String salvarEdicaoSubInstituicao(
            @org.springframework.web.bind.annotation.ModelAttribute SubInstituicao subInstituicao, HttpSession session,
            Model model) {
        Instituicao instituicaoSelecionada = (Instituicao) session.getAttribute("instituicaoSelecionada");
        if (instituicaoSelecionada == null) {
            model.addAttribute("mensagemErro", "Instituição não selecionada.");
            return "redirect:/administrador/subinstituicoes";
        }
        subInstituicao.setInstituicao(instituicaoSelecionada);
        subInstituicao.setDataUltimaAtualizacao(java.time.LocalDate.now());
        subInstituicaoRepository.save(subInstituicao);
        return "redirect:/administrador/subinstituicoes";
    }

    @org.springframework.web.bind.annotation.PostMapping("/administrador/subinstituicoes/excluir/{id}")
    public String excluirSubInstituicao(@org.springframework.web.bind.annotation.PathVariable Long id) {
        subInstituicaoRepository.deleteById(id);
        return "redirect:/administrador/subinstituicoes";
    }
}
