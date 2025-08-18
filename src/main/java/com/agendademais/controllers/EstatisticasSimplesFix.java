package com.agendademais.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/administrador")
public class EstatisticasSimplesFix {

    @GetMapping("/estatistica-usuarios-simples")
    public String testeSimples(Model model) {
        System.out.println("*** TESTE SIMPLES: Página acessada ***");

        model.addAttribute("totalUsuarios", 10);
        model.addAttribute("totalInstituicoes", 3);
        model.addAttribute("totalVinculos", 25);
        model.addAttribute("mensagem", "Teste básico funcionando!");

        return "gestao-usuarios/estatistica-usuarios";
    }
}
