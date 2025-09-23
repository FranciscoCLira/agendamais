package com.agendademais.controllers;

import com.agendademais.entities.UsuarioInstituicao;
import com.agendademais.repositories.UsuarioInstituicaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.List;

/**
 * Controller de estatísticas SUPER simplificado para debug
 */
@Controller
@RequestMapping("/administrador")
public class EstatisticasSuperSimpleController {

    @Autowired
    private UsuarioInstituicaoRepository usuarioInstituicaoRepository;

    @GetMapping("/estatistica-super-simples")
    public String estatisticasSuperSimples(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        System.out.println("*** DEBUG SUPER SIMPLES: Iniciando ***");
        System.out.println("*** DEBUG SUPER SIMPLES: Session ID: " + session.getId());

        // Verificar sessão básica
        Object usuarioLogado = session.getAttribute("usuarioLogado");
        System.out.println("*** DEBUG SUPER SIMPLES: usuarioLogado = " + (usuarioLogado != null ? "existe" : "null"));

        if (usuarioLogado == null) {
            System.out.println("*** DEBUG SUPER SIMPLES: Redirecionando para /acesso ***");
            redirectAttributes.addFlashAttribute("mensagemErro", "Sessão perdida. Faça login novamente.");
            return "redirect:/acesso";
        }

        // Buscar dados mínimos
        System.out.println("*** DEBUG SUPER SIMPLES: Buscando dados ***");
        List<UsuarioInstituicao> usuarios = usuarioInstituicaoRepository.findAll();
        System.out.println("*** DEBUG SUPER SIMPLES: Encontrados " + usuarios.size() + " usuários ***");

        // Dados mínimos
        model.addAttribute("totalUsuarios", usuarios.size());
        model.addAttribute("mensagemSucesso", "Controller super simples funcionando!");

        System.out.println("*** DEBUG SUPER SIMPLES: Retornando template ***");
        return "administrador/estatistica-super-simples";
    }
}
