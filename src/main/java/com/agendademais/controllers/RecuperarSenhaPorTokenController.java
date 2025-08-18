package com.agendademais.controllers;

import com.agendademais.entities.Usuario;
import com.agendademais.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
public class RecuperarSenhaPorTokenController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/recuperar-senha-por-token")
    public String exibirFormulario(@RequestParam("token") String token, Model model) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByTokenRecuperacao(token);

        if (usuarioOpt.isEmpty() || usuarioOpt.get().getDataExpiracaoToken() == null ||
                usuarioOpt.get().getDataExpiracaoToken().isBefore(LocalDateTime.now())) {
            return "recuperar-senha-expirado";
        }

        model.addAttribute("token", token);
        return "recuperar-senha-token";
    }

    @PostMapping("/recuperar-senha-por-token")
    public String processarRecuperacao(@RequestParam String token,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByTokenRecuperacao(token);

        if (usuarioOpt.isEmpty() || usuarioOpt.get().getDataExpiracaoToken() == null ||
                usuarioOpt.get().getDataExpiracaoToken().isBefore(LocalDateTime.now())) {
            model.addAttribute("mensagemErro", "Link expirado ou inválido.");
            return "recuperar-senha-expirado";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("mensagemErro", "As senhas não coincidem.");
            model.addAttribute("token", token);
            return "recuperar-senha-token";
        }

        if (!isSenhaSegura(newPassword)) {
            model.addAttribute("mensagemErro",
                    "A senha deve ter no mínimo 6 caracteres e conter letras, números ou símbolos.");
            model.addAttribute("token", token);
            return "recuperar-senha-token";
        }

        Usuario usuario = usuarioOpt.get();
        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuario.setTokenRecuperacao(null); // Invalida token após uso
        usuario.setDataExpiracaoToken(null);
        usuarioRepository.save(usuario);

        model.addAttribute("mensagemSucesso", "Senha redefinida com sucesso.");
        return "login";
    }

    private boolean isSenhaSegura(String senha) {
        if (senha == null || senha.length() < 6)
            return false;
        return senha.matches(".*[a-zA-Z].*") && (senha.matches(".*\\d.*") || senha.matches(".*\\W.*"));
    }
}
