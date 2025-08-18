package com.agendademais.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/recuperar-senha")
    public String exibirFormulario() {
        return "recuperar-senha";
    }

    @PostMapping("/recuperar-senha")
    public String processarRecuperacao(@RequestParam String username,
                                       @RequestParam String newPassword,
                                       @RequestParam String confirmPassword,
                                       Model model) {

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("mensagemErro", "As senhas não coincidem: " + username);
            model.addAttribute("username", username);
            model.addAttribute("newPassword", newPassword);
            model.addAttribute("confirmPassword", confirmPassword);
            return "recuperar-senha";
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);

        if (usuarioOpt.isEmpty()) {
            model.addAttribute("mensagemErro", "Usuário não encontrado: " + username);
            model.addAttribute("username", username);
            model.addAttribute("newPassword", newPassword);
            model.addAttribute("confirmPassword", confirmPassword);
            return "recuperar-senha";
        }
        
        if (!ispasswordSegura(newPassword)) {
            model.addAttribute("mensagemErro", "A senha deve ter no mínimo 6 caracteres e conter letras, números ou símbolos.");
            model.addAttribute("username", username);
            model.addAttribute("newPassword", newPassword);
            model.addAttribute("confirmPassword", confirmPassword);
            return "recuperar-senha";
        }
        
        Usuario usuario = usuarioOpt.get();

        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);
        model.addAttribute("mensagemSucesso", "Senha redefinida com sucesso! Usuário: " + username);

        // Limpa campos após sucesso
        model.addAttribute("username", "");
        model.addAttribute("newPassword", "");
        model.addAttribute("confirmPassword", "");

        return "recuperar-senha";
    }
    
    private boolean ispasswordSegura(String password) {
        if (password == null || password.length() < 6) return false;
        return password.matches(".*[a-zA-Z].*") && (password.matches(".*\\d.*") || password.matches(".*\\W.*"));
    }


}
