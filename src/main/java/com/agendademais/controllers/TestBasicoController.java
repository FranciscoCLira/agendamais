package com.agendademais.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;

/**
 * Controller ultra-simples para teste de debug
 */
@Controller
@RequestMapping("/administrador")
public class TestBasicoController {

    @GetMapping("/test-basico")
    public String testBasico(HttpSession session, Model model) {
        System.out.println("*** TEST BASICO: Iniciando ***");

        // Verificar sessão
        Object usuarioLogado = session.getAttribute("usuarioLogado");
        System.out.println("*** TEST BASICO: Usuario logado na sessão: " + (usuarioLogado != null ? "existe" : "null"));

        // Verificar se sessão é nula
        // if (session == null) {
        // System.out.println("*** TEST BASICO: Session é null ***");
        // return "redirect:/acesso";
        // }

        // Verificar se sessão tem ID
        System.out.println("*** TEST BASICO: Session ID: " + session.getId());

        // Listar todos os atributos da sessão
        System.out.println("*** TEST BASICO: Atributos da sessão: ***");
        session.getAttributeNames().asIterator().forEachRemaining(name -> {
            System.out.println("  " + name + " = " + session.getAttribute(name));
        });

        // Retornar uma página simples inline
        model.addAttribute("sessionId", session.getId());
        model.addAttribute("usuarioLogado", usuarioLogado);
        model.addAttribute("isNewSession", session.isNew());

        System.out.println("*** TEST BASICO: Retornando template inline ***");

        // Em vez de template externo, vamos usar uma string inline
        return "administrador/test-basico";
    }
}
