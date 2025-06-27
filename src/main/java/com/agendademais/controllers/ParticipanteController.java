package com.agendademais.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ParticipanteController {

    @GetMapping("/participante-form")
    public String exibirFormularioParticipante() {
        return "participante-form";
    }
}
