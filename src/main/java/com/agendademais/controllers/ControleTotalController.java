
package com.agendademais.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/menus")
public class ControleTotalController {

    @GetMapping("/menu-controle-total-xxxxx")
    public String mostrarMenuControleTotal(Model model) {
        model.addAttribute("tituloPagina", "Painel de Controle Total");
        return "menus/menu-controle-total";
    }
}
