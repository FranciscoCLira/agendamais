package com.agendademais.controllers;

import com.agendademais.entities.UsuarioInstituicao;
import com.agendademais.repositories.UsuarioInstituicaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import java.util.List;

/**
 * Versão simplificada do controller de estatísticas para debug
 */
@Controller
@RequestMapping("/administrador")
public class EstatisticaSimpleController {

    @Autowired
    private UsuarioInstituicaoRepository usuarioInstituicaoRepository;

    @GetMapping("/estatistica-simples")
    public String estatisticasSimples(HttpSession session, Model model) {
        try {
            System.out.println("*** DEBUG SIMPLES: Iniciando ***");
            
            // Verificar sessão
            Object usuarioLogado = session.getAttribute("usuarioLogado");
            System.out.println("*** DEBUG SIMPLES: Usuario logado: " + (usuarioLogado != null ? "existe" : "null"));
            
            if (usuarioLogado == null) {
                System.out.println("*** DEBUG SIMPLES: Sessão inválida ***");
                return "redirect:/acesso";
            }
            
            // Buscar usuários básico
            System.out.println("*** DEBUG SIMPLES: Buscando usuários ***");
            List<UsuarioInstituicao> usuarios = usuarioInstituicaoRepository.findAll();
            System.out.println("*** DEBUG SIMPLES: Encontrados " + usuarios.size() + " usuários ***");
            
            // Dados básicos
            model.addAttribute("totalUsuarios", usuarios.size());
            model.addAttribute("usuarios", usuarios);
            model.addAttribute("usuarioLogado", usuarioLogado);
            
            System.out.println("*** DEBUG SIMPLES: Sucesso - template simples ***");
            return "administrador/estatistica-simples";
            
        } catch (Exception e) {
            System.out.println("*** DEBUG SIMPLES: ERRO: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/administrador";
        }
    }
}
