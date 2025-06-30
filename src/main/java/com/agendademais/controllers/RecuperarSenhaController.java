package com.agendademais.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.agendademais.entities.Usuario;
import com.agendademais.repositories.UsuarioRepository;

import org.springframework.ui.Model;

@Controller
public class RecuperarSenhaController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/recuperar-senha")
    public String exibirFormulario() {
        return "recuperar-senha";
    }

    @PostMapping("/recuperar-senha")
    public String processarRecuperacao(@RequestParam String codUsuario,
                                       @RequestParam String novaSenha,
                                       @RequestParam String confirmarSenha,
                                       Model model) {

        if (!novaSenha.equals(confirmarSenha)) {
            model.addAttribute("mensagem", "As senhas não coincidem: " + codUsuario);
            model.addAttribute("codUsuario", codUsuario);
            model.addAttribute("novaSenha", novaSenha);
            model.addAttribute("confirmarSenha", confirmarSenha);
            return "recuperar-senha";
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByCodUsuario(codUsuario);

        if (usuarioOpt.isEmpty()) {
            model.addAttribute("mensagem", "Usuário não encontrado: " + codUsuario);
            model.addAttribute("codUsuario", codUsuario);
            model.addAttribute("novaSenha", novaSenha);
            model.addAttribute("confirmarSenha", confirmarSenha);
            return "recuperar-senha";
        }

        Usuario usuario = usuarioOpt.get();

        usuario.setSenha(novaSenha);
        usuarioRepository.save(usuario);
        model.addAttribute("mensagem", "Senha redefinida com sucesso! Usuário: " + codUsuario);

        // Limpa campos após sucesso
        model.addAttribute("codUsuario", "");
        model.addAttribute("novaSenha", "");
        model.addAttribute("confirmarSenha", "");

        return "recuperar-senha";
    }

}
