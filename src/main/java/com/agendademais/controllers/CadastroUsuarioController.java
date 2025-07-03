package com.agendademais.controllers;

import com.agendademais.entities.Usuario;
import com.agendademais.repositories.UsuarioRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class CadastroUsuarioController {

    private final UsuarioRepository usuarioRepository;

    public CadastroUsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/cadastro-usuario")
    public String mostrarFormularioCadastroUsuario(Model model) {
    	// limpa mensagem
        // model.addAttribute("mensagemSucesso", null);
        return "cadastro-usuario";
    }

    @PostMapping("/cadastro-usuario")
    public String processarCadastroUsuario(
            @RequestParam String codUsuario,
            @RequestParam String senha,
            @RequestParam String confirmarSenha,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!senha.equals(confirmarSenha)) {
            model.addAttribute("mensagemErro", "As senhas não coincidem.");
            model.addAttribute("codUsuario", codUsuario);
            return "cadastro-usuario";
        }

        Optional<Usuario> existente = usuarioRepository.findByCodUsuario(codUsuario);
        if (existente.isPresent()) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                "Usuário já iniciado, faça login e conclua seu cadastro.");
            return "redirect:/login";
        }

        return "redirect:/cadastro-pessoa?codUsuario=" + codUsuario + "&senha=" + senha;
    }
}
