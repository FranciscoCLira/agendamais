package com.agendademais.controllers;

import com.agendademais.entities.Usuario;
import com.agendademais.repositories.UsuarioRepository;
import com.agendademais.repositories.UsuarioInstituicaoRepository;
import com.agendademais.entities.UsuarioInstituicao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class LoginController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioInstituicaoRepository usuarioInstituicaoRepository;

    @GetMapping("/")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String codUsuario,
                        @RequestParam String senha,
                        @RequestParam Long idInstituicao,
                        Model model) {

        Optional<Usuario> usuarioOpt = usuarioRepository.findByCodUsuarioAndSenha(codUsuario, senha);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            Optional<UsuarioInstituicao> vinculo = usuarioInstituicaoRepository.findByUsuarioIdAndInstituicaoId(usuario.getId(), idInstituicao);
            if (vinculo.isPresent()) {
                model.addAttribute("nomeInstituicao", vinculo.get().getInstituicao().getNomeInstituicao());

                switch (usuario.getNivelAcessoUsuario()) {
                    case 1:
                        return "redirect:/participante-form";
                    case 2:
                        return "redirect:/autor-form";
                    case 5:
                        return "redirect:/administrador-form";
                    case 9:
                        return "redirect:/superusuario-form";
                    default:
                        return "redirect:/erro";
                }
            }
        }

        model.addAttribute("erro", "Usuário ou senha inválidos, ou instituição não vinculada.");
        return "login";
    }

    @GetMapping("/cadastro")
    public String showCadastroForm() {
        return "cadastro-form";
    }
}        
