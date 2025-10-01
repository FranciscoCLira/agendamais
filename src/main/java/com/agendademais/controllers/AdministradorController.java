package com.agendademais.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdministradorController {

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
        // Aqui futuramente pode-se adicionar lógica para filtrar subinstituições da instituição logada
        return "administrador/gerenciar-subinstituicoes";
    }
}
