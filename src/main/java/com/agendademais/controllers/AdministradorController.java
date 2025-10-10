package com.agendademais.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.List;

import com.agendademais.repositories.PessoaSubInstituicaoRepository;
import com.agendademais.repositories.SubInstituicaoRepository;
import com.agendademais.entities.Instituicao;
import com.agendademais.entities.SubInstituicao;

@Controller
public class AdministradorController {

    @Autowired
    private SubInstituicaoRepository subInstituicaoRepository;

    @Autowired
    private PessoaSubInstituicaoRepository pessoaSubInstituicaoRepository;

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

    @PostMapping("/administrador/subinstituicoes/nova")
    public String novaSubInstituicao(
            @ModelAttribute SubInstituicao subInstituicao,
            @RequestParam Map<String, String> params,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        Instituicao instituicaoSelecionada = (Instituicao) session.getAttribute("instituicaoSelecionada");
        if (instituicaoSelecionada == null) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Instituição não selecionada.");
            return buildRedirectUrlWithParams(params);
        }
        subInstituicao.setInstituicao(instituicaoSelecionada);
        subInstituicao.setDataUltimaAtualizacao(java.time.LocalDate.now());
        subInstituicaoRepository.save(subInstituicao);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Sub-Instituição criada com sucesso.");
        return buildRedirectUrlWithParams(params);
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

    @PostMapping("/administrador/subinstituicoes/editar")
    public String salvarEdicaoSubInstituicao(
            @ModelAttribute SubInstituicao subInstituicao,
            @RequestParam Map<String, String> params,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        Instituicao instituicaoSelecionada = (Instituicao) session.getAttribute("instituicaoSelecionada");
        if (instituicaoSelecionada == null) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Instituição não selecionada.");
            return buildRedirectUrlWithParams(params);
        }
        subInstituicao.setInstituicao(instituicaoSelecionada);
        subInstituicao.setDataUltimaAtualizacao(java.time.LocalDate.now());
        subInstituicaoRepository.save(subInstituicao);
        return buildRedirectUrlWithParams(params);
    }

    @org.springframework.web.bind.annotation.PostMapping("/administrador/subinstituicoes/excluir/{id}")
    public String excluirSubInstituicao(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            @RequestParam Map<String, String> params,
            RedirectAttributes redirectAttributes) {
        long count = pessoaSubInstituicaoRepository.countBySubInstituicaoId(id);
        if (count > 0) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Não é possível excluir: existem pessoas vinculadas a esta Sub-Instituição.");
            return buildRedirectUrlWithParams(params);
        }
        subInstituicaoRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Sub-Instituição excluída com sucesso.");
        return buildRedirectUrlWithParams(params);
    }

    private String buildRedirectUrlWithParams(Map<String, String> params) {
        StringBuilder redirectUrl = new StringBuilder("redirect:/administrador/subinstituicoes");
        if (!params.isEmpty()) {
            redirectUrl.append("?");
            params.forEach((k, v) -> {
                if (v != null && !v.isEmpty())
                    redirectUrl.append(k).append("=").append(URLEncoder.encode(v, StandardCharsets.UTF_8)).append("&");
            });
            redirectUrl.setLength(redirectUrl.length() - 1); // Remove last &
        }
        System.out.println("DEBUG Redirecting to: " + redirectUrl.toString());
        return redirectUrl.toString();
    }
}
