package com.agendademais.config;

import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute
    public void adicionarVariaveisGlobais(HttpSession session, Model model) {
    	
        System.out.println("**************************************");
        System.out.println("✅ GlobalModelAttributes executando...");
        System.out.println("**************************************");
    	
        Object usuario = session.getAttribute("usuarioLogado");
        Object instituicao = session.getAttribute("instituicaoSelecionada");

        model.addAttribute("usuarioLogado", usuario);
        model.addAttribute("instituicaoSelecionada", instituicao);
    }
}

