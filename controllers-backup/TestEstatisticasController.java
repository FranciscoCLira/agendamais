package com.agendademais.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller mínimo para testar a rota de estatísticas
 */
@Controller
@RequestMapping("/administrador")
public class TestEstatisticasController {

    @GetMapping("/test-estatisticas")
    public String testEstatisticas(Model model) {
        System.out.println("*** TEST ESTATISTICAS: FUNCIONANDO! ***");
        
        model.addAttribute("mensagem", "Rota funcionando!");
        
        // Retornar template simples inline
        return "administrador/test-estatisticas";
    }
}
