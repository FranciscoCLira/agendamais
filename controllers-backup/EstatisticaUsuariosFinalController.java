package com.agendademais.controllers;

import com.agendademais.entities.Usuario;
import com.agendademais.entities.Instituicao;
import com.agendademais.entities.UsuarioInstituicao;
import com.agendademais.repositories.UsuarioInstituicaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller de Estatísticas de Usuários - Versão Final
 */
@Controller
@RequestMapping("/administrador")
public class EstatisticaUsuariosFinalController {

    @Autowired
    private UsuarioInstituicaoRepository usuarioInstituicaoRepository;

    @GetMapping("/estatistica-usuarios")
    public String estatisticasUsuarios(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        
        try {
            System.out.println("*** DEBUG FINAL: INÍCIO DO MÉTODO ***");
            
            // Teste simples primeiro
            model.addAttribute("totalUsuarios", 5);
            model.addAttribute("totalInstituicoes", 2);
            model.addAttribute("totalVinculos", 10);
            model.addAttribute("mensagem", "Teste básico funcionando!");
            model.addAttribute("nivelAcessoLogado", 9);
            
            System.out.println("*** DEBUG FINAL: RETORNANDO TEMPLATE ***");
            return "gestao-usuarios/estatistica-usuarios";

        } catch (Exception e) {
            System.out.println("*** ERRO CRÍTICO no EstatisticaUsuariosFinalController ***");
            System.out.println("Erro: " + e.getMessage());
            e.printStackTrace();
            
            model.addAttribute("erro", "Erro crítico: " + e.getMessage());
            model.addAttribute("totalUsuarios", 0);
            model.addAttribute("totalVinculos", 0);
            return "gestao-usuarios/estatistica-usuarios";
        }
    }
    
    private String getNivelTexto(Integer nivel) {
        if (nivel == null) return "Não definido";
        switch (nivel) {
            case 1: return "Participante";
            case 2: return "Autor";
            case 5: return "Administrador";
            case 9: return "SuperUsuário";
            default: return "Nível " + nivel;
        }
    }
    
    private String getStatusTexto(String situacao) {
        if (situacao == null) return "Não definido";
        switch (situacao.toUpperCase()) {
            case "A": return "Ativo";
            case "B": return "Bloqueado";
            case "C": return "Cancelado";
            default: return "Status: " + situacao;
        }
    }
}
