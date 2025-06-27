package com.agendademais.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AutorController {

    @GetMapping("/autor-form")
    public String exibirFormularioAutor(HttpSession session, Model model) {
        String nomeInstituicao = (String) session.getAttribute("nomeInstituicao");
        model.addAttribute("nomeInstituicao", nomeInstituicao);
        return "autor-form";
    }
}
