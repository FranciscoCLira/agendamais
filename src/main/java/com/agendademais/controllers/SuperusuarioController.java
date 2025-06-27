package com.agendademais.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SuperusuarioController {

    @GetMapping("/superusuario-form")
    public String exibirFormularioSuperusuario(HttpSession session, Model model) {
        String nomeInstituicao = (String) session.getAttribute("nomeInstituicao");
        model.addAttribute("nomeInstituicao", nomeInstituicao);
        return "superusuario-form";
    }
}
