package com.agendademais.controllers;

import com.agendademais.entities.Usuario;
import com.agendademais.repositories.UsuarioRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/cadastro-usuario")
public class CadastroUsuarioController {

    private final UsuarioRepository usuarioRepository;

    public CadastroUsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public String mostrarFormularioCadastroUsuario(Model model) {
        return "cadastro-usuario";
    }

    @PostMapping
    public String processarCadastroUsuario(@RequestParam String codUsuario,
                                           @RequestParam String senha,
                                           @RequestParam String confirmarSenha,
                                           RedirectAttributes redirectAttributes,
                                           Model model) {

        if (codUsuario.length() < 6 || codUsuario.length() > 25) {
            model.addAttribute("mensagemErro", "O Código do Usuário deve ter entre 6 e 25 caracteres.");
            model.addAttribute("codUsuario", codUsuario);
            return "cadastro-usuario";
        }

        if (!senha.equals(confirmarSenha)) {
            model.addAttribute("mensagemErro", "As senhas não coincidem.");
            model.addAttribute("codUsuario", codUsuario);
            return "cadastro-usuario";
        }

        Optional<Usuario> existente = usuarioRepository.findByCodUsuario(codUsuario);
        if (existente.isPresent()) {
            Usuario usuario = existente.get();
            boolean temPessoa = usuario.getPessoa() != null;
            if (!temPessoa) {
                redirectAttributes.addFlashAttribute("mensagemErro",
                        "Usuário já iniciado. Faça login para concluir seu cadastro.");
                return "redirect:/login";
            } else {
                model.addAttribute("mensagemErro", "Já existe um usuário com esse código.");
                return "cadastro-usuario";
            }
        }

        return "redirect:/cadastro-pessoa?codUsuario=" + codUsuario + "&senha=" + senha;
    }
}
