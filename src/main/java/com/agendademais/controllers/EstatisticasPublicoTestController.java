package com.agendademais.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller de teste público para validar template
 */
@Controller
@RequestMapping("/publico")
public class EstatisticasPublicoTestController {

    @GetMapping("/estatisticas-teste")
    public String testePublico(Model model) {

        try {
            System.out.println("*** TESTE PÚBLICO: Iniciando ***");

            // Dados de teste
            model.addAttribute("totalUsuarios", 25);
            model.addAttribute("totalInstituicoes", 3);
            model.addAttribute("totalVinculos", 50);
            model.addAttribute("mensagem", "Teste público funcionando - Template OK!");
            model.addAttribute("nivelAcessoLogado", 9);

            // Dados simples para charts
            model.addAttribute("niveisLabels", java.util.Arrays.asList("Participante", "Administrador"));
            model.addAttribute("niveisValues", java.util.Arrays.asList(15L, 10L));

            model.addAttribute("statusLabels", java.util.Arrays.asList("Ativo", "Inativo"));
            model.addAttribute("statusValues", java.util.Arrays.asList(20L, 5L));

            System.out.println("*** TESTE PÚBLICO: Template será renderizado ***");
            return "gestao-usuarios/estatistica-usuarios";

        } catch (Exception e) {
            System.out.println("*** ERRO no Teste Público ***");
            System.out.println("Erro: " + e.getMessage());
            e.printStackTrace();

            return "error/generic";
        }
    }
}
