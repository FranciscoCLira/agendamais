package com.agendademais.controllers;

import com.agendademais.entities.Usuario;
import com.agendademais.repositories.UsuarioRepository;
import com.agendademais.repositories.InstituicaoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class LoginController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private InstituicaoRepository instituicaoRepository;

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("instituicoes", instituicaoRepository.findAll());
        return "login";
    }

    @PostMapping("/login")
    public String processarLogin(@RequestParam String codUsuario,
                                  @RequestParam String senha,
                                  HttpSession session,
                                  Model model) {
        Optional<Usuario> optUsuario = usuarioRepository.findByCodUsuario(codUsuario);

        if (optUsuario.isPresent() && optUsuario.get().getSenha().equals(senha)) {
            Usuario usuario = optUsuario.get();
            session.setAttribute("usuarioLogado", usuario);
            switch (usuario.getNivelAcessoUsuario()) {
                case 1: return "redirect:/participante-form";
                case 2: return "redirect:/autor-form";
                case 5: return "redirect:/administrador-form";
                case 9: return "redirect:/superusuario-form";
                default: return "redirect:/login";
            }
        } else {
            model.addAttribute("erro", "Usuário ou senha inválidos");
            model.addAttribute("instituicoes", instituicaoRepository.findAll());
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
