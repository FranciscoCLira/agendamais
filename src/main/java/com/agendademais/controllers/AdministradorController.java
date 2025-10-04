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
        return "administrador/gerenciar-subinstituicoes";
    }
}
