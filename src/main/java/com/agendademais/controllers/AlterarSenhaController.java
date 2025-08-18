package com.agendademais.controllers;

import com.agendademais.entities.Usuario;
import com.agendademais.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class AlterarSenhaController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/alterar-senha")
    public String exibirFormulario() {
        return "alterar-senha";
    }

    @PostMapping("/alterar-senha")
    public String processarAlteracao(@RequestParam String username,
                                     @RequestParam String currentPassword,
                                     @RequestParam String newPassword,
                                     @RequestParam String confirmPassword,
                                     Model model) {

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("mensagemErro", "As senhas não coincidem: " + username);
            model.addAttribute("username", username);
            model.addAttribute("newPassword", newPassword);
            model.addAttribute("confirmPassword", confirmPassword);
            return "alterar-senha";
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);

        if (usuarioOpt.isEmpty()) {
            model.addAttribute("mensagemErro", "Usuário não encontrado: " + username);
            model.addAttribute("username", username);
            model.addAttribute("newPassword", newPassword);
            model.addAttribute("confirmPassword", confirmPassword);
            return "alterar-senha";
        }

        if (!isPasswordSegura(newPassword)) {
            model.addAttribute("mensagemErro", "A nova senha deve ter no mínimo 6 caracteres e conter letras, números ou símbolos.");
            model.addAttribute("username", username);
            model.addAttribute("newPassword", newPassword);
            model.addAttribute("confirmPassword", confirmPassword);
            return "alterar-senha";
        }
        
        Usuario usuario = usuarioOpt.get();

        if (!passwordEncoder.matches(currentPassword, usuario.getPassword())) {
            model.addAttribute("mensagemErro", "Senha atual incorreta: " + username);
            model.addAttribute("username", username);
            model.addAttribute("newPassword", newPassword);
            model.addAttribute("confirmPassword", confirmPassword);
            return "alterar-senha";
        }

        if (passwordEncoder.matches(newPassword, usuario.getPassword())) {
            model.addAttribute("mensagemErro", "A nova senha não pode ser igual à anterior: " + username);
            model.addAttribute("username", username);
            model.addAttribute("newPassword", newPassword);
            model.addAttribute("confirmPassword", confirmPassword);
            return "alterar-senha";
        }

        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);
        model.addAttribute("mensagemSucesso", "Senha alterada com sucesso! Usuário: " + username);

        // Limpa campos após sucesso
        model.addAttribute("username", "");
        model.addAttribute("newPassword", "");
        model.addAttribute("confirmPassword", "");

        return "alterar-senha";
    }
    
    private boolean isPasswordSegura(String password) {
        if (password == null || password.length() < 6) return false;
        return password.matches(".*[a-zA-Z].*") && (password.matches(".*\\d.*") || password.matches(".*\\W.*"));
    }


}
