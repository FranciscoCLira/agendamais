package com.agendademais.controllers;

import com.agendademais.entities.Usuario;
import com.agendademais.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class AlterarSenhaController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/alterar-senha")
    public String exibirFormulario() {
        return "alterar-senha";
    }

    @PostMapping("/alterar-senha")
    public String processarAlteracao(@RequestParam String codUsuario,
                                     @RequestParam String senhaAtual,
                                     @RequestParam String novaSenha,
                                     @RequestParam String confirmarSenha,
                                     Model model) {

        if (!novaSenha.equals(confirmarSenha)) {
            model.addAttribute("mensagemErro", "As senhas não coincidem: " + codUsuario);
            model.addAttribute("codUsuario", codUsuario);
            model.addAttribute("novaSenha", novaSenha);
            model.addAttribute("confirmarSenha", confirmarSenha);
            return "alterar-senha";
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByCodUsuario(codUsuario);

        if (usuarioOpt.isEmpty()) {
            model.addAttribute("mensagemErro", "Usuário não encontrado: " + codUsuario);
            model.addAttribute("codUsuario", codUsuario);
            model.addAttribute("novaSenha", novaSenha);
            model.addAttribute("confirmarSenha", confirmarSenha);
            return "alterar-senha";
        }

        Usuario usuario = usuarioOpt.get();

        if (!usuario.getSenha().equals(senhaAtual)) {
            model.addAttribute("mensagemErro", "Senha atual incorreta: " + codUsuario);
            model.addAttribute("codUsuario", codUsuario);
            model.addAttribute("novaSenha", novaSenha);
            model.addAttribute("confirmarSenha", confirmarSenha);
            return "alterar-senha";
        }

        if (usuario.getSenha().equals(novaSenha)) {
            model.addAttribute("mensagemErro", "A nova senha não pode ser igual à anterior: " + codUsuario);
            model.addAttribute("codUsuario", codUsuario);
            model.addAttribute("novaSenha", novaSenha);
            model.addAttribute("confirmarSenha", confirmarSenha);
            return "alterar-senha";
        }

        usuario.setSenha(novaSenha);
        usuarioRepository.save(usuario);
        model.addAttribute("mensagemErro", "Senha alterada com sucesso! Usuário: " + codUsuario);

        // Limpa campos após sucesso
        model.addAttribute("codUsuario", "");
        model.addAttribute("novaSenha", "");
        model.addAttribute("confirmarSenha", "");

        return "alterar-senha";
    }

}
