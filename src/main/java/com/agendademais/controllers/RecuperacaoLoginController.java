package com.agendademais.controllers;

import com.agendademais.entities.Usuario;
import com.agendademais.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class RecuperacaoLoginController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/recuperar-login")
    public String recuperarLogin(@RequestParam("emailRecuperacao") String email, Model model) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByPessoaEmailPessoa(email);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            model.addAttribute("mensagem", "Seu código de usuário é: " + usuario.getCodUsuario() + " e sua senha é: " + usuario.getSenha());
        } else {
            model.addAttribute("mensagem", "Email não encontrado. Verifique se digitou corretamente ou entre em contato com o administrador.");
        }
        return "login";
    }
}
