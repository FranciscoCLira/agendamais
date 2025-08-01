package com.agendademais.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller para páginas principais da aplicação
 */
@Controller
public class HomeController {

    /**
     * Página inicial - redireciona para o login
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/acesso";
    }

    /**
     * Página index - redireciona para o login
     */
    @GetMapping("/index")
    public String index() {
        return "redirect:/acesso";
    }
}
