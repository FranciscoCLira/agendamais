package com.agendademais.controllers;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.agendademais.entities.Local;
import com.agendademais.entities.Usuario;
import com.agendademais.entities.UsuarioInstituicao;
import com.agendademais.entities.Instituicao;
import com.agendademais.repositories.UsuarioInstituicaoRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/administrador")
public class EstatisticaUsuariosController {

    @Autowired
    private UsuarioInstituicaoRepository usuarioInstituicaoRepository;

    @GetMapping("/stats-usuarios-final")
    public String mostrarEstatisticas(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            System.out.println("*** DEBUG EstatisticaUsuarios: INÍCIO DO MÉTODO ***");

            // Teste básico
            model.addAttribute("mensagem", "Página de estatísticas funcionando!");

            System.out.println("*** DEBUG EstatisticaUsuarios: RETORNANDO TEMPLATE ***");
            return "gestao-usuarios/estatistica-usuarios";

        } catch (Exception e) {
            System.out.println("*** ERRO no EstatisticaUsuariosController ***");
            System.out.println("Erro: " + e.getMessage());
            e.printStackTrace();

            model.addAttribute("erro", "Erro ao carregar estatísticas: " + e.getMessage());
            return "admin/estatistica-usuarios";
        }
    }
}