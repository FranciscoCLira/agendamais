package com.agendademais.controllers;

import com.agendademais.entities.Usuario;
import com.agendademais.repositories.UsuarioRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    public String processarCadastroUsuario(@RequestParam String codUsuario,
                                           @RequestParam String senha,
                                           @RequestParam String confirmarSenha,
                                           Model model) {

        if (!senha.equals(confirmarSenha)) {
            model.addAttribute("mensagemErro", "As senhas não coincidem.");
            model.addAttribute("codUsuario", codUsuario);
            return "cadastro-usuario";
        }

        Optional<Usuario> existente = usuarioRepository.findByCodUsuario(codUsuario);
        if (existente.isPresent()) {
            model.addAttribute("mensagemErro", "Já existe um usuário com esse código. Tente outro.");
            model.addAttribute("codUsuario", codUsuario);
            return "cadastro-usuario";
        }

        return "redirect:/cadastro-pessoa?codUsuario=" + codUsuario + "&senha=" + senha;
    }
}
