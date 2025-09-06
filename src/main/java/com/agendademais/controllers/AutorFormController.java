package com.agendademais.controllers;

import com.agendademais.entities.Autor;
import com.agendademais.repositories.AutorRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AutorFormController {

    @Autowired
    private AutorRepository autorRepository;

    @GetMapping("/administrador/autor-form/{id}")
    public String exibirAutorForm(@PathVariable Long id, Model model, HttpSession session,
            RedirectAttributes redirectAttributes) {
        Optional<Autor> autorOpt = autorRepository.findById(id);
        if (autorOpt.isPresent()) {
            model.addAttribute("autor", autorOpt.get());
            return "administrador/autor-form";
        } else {
            redirectAttributes.addFlashAttribute("mensagemErro", "Autor não encontrado.");
            return "redirect:/gestao-usuarios/lista-usuarios";
        }
    }
}
