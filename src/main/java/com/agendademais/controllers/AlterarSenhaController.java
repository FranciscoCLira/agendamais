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
public class AlterarSenhaController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/alterar-senha")
    public String alterarSenha(@RequestParam String codUsuario,
                                @RequestParam String senhaAtual,
                                @RequestParam String novaSenha,
                                Model model) {

        Optional<Usuario> usuarioOpt = usuarioRepository.findByCodUsuario(codUsuario);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (usuario.getSenha().equals(senhaAtual)) {
                usuario.setSenha(novaSenha);
                usuarioRepository.save(usuario);
                model.addAttribute("mensagem", "Senha alterada com sucesso.");
            } else {
                model.addAttribute("mensagem", "Senha atual incorreta.");
            }
        } else {
            model.addAttribute("mensagem", "Usuário não encontrado.");
        }

        return "alterar-senha";
    }
}
