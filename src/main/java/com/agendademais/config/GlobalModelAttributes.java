package com.agendademais.config;

import com.agendademais.entities.Instituicao;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute
    public void adicionarVariaveisGlobais(HttpSession session, Model model) {

        System.out.println("*** DEBUG GlobalModelAttributes ***");
        System.out.println("Sessão ID: " + session.getId());

        Object usuario = session.getAttribute("usuarioLogado");
        Object usuarioCadastro = session.getAttribute("usuarioCadastro");
        Object inst = session.getAttribute("instituicaoSelecionada");

        System.out.println("usuarioLogado: " + (usuario != null ? "existe" : "null"));
        System.out.println("usuarioCadastro: " + (usuarioCadastro != null ? "existe" : "null"));
        System.out.println("instituicaoSelecionada: " + (inst != null ? "existe" : "null"));

        model.addAttribute("usuarioLogado", usuario);

        if (inst instanceof Instituicao) {
            model.addAttribute("instituicaoSelecionada", (Instituicao) inst);
        } else {
            model.addAttribute("instituicaoSelecionada", null);
            model.addAttribute("controleTotal", true);
        }
    }
}
