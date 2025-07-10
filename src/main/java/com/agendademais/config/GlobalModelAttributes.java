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
    	
        // System.out.println("**************************************");
        // System.out.println("✅ GlobalModelAttributes EXECUTANDO...");
        // System.out.println("**************************************");

        Object usuario = session.getAttribute("usuarioLogado");
        Object inst = session.getAttribute("instituicaoSelecionada");

        model.addAttribute("usuarioLogado", usuario);

        if (inst instanceof Instituicao) {
            model.addAttribute("instituicaoSelecionada", (Instituicao) inst);
        } else {
            model.addAttribute("instituicaoSelecionada", null);
            model.addAttribute("controleTotal", true);
        }
    }
}
