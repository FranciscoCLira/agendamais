package com.agendademais.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/administrador")
public class EstatisticaTesteController {

    @GetMapping("/debug-isolado")
    public String debugController(Model model, HttpSession session) {
        System.out.println("*** TESTE DEBUG CONTROLLER FUNCIONANDO ***");
        System.out.println("Session ID: " + session.getId());

        model.addAttribute("mensagem", "Controller de teste funcionando!");
        model.addAttribute("sessionId", session.getId());

        return "administrador/test-basico"; // Reutilizar um template que sabemos que funciona
    }
}
