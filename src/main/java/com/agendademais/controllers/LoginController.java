package com.agendademais.controllers;

import com.agendademais.entities.Usuario;
import com.agendademais.entities.Instituicao;
import com.agendademais.repositories.UsuarioRepository;
import com.agendademais.repositories.UsuarioInstituicaoRepository;
import com.agendademais.repositories.InstituicaoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
public class LoginController {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioInstituicaoRepository usuarioInstituicaoRepository;
    private final InstituicaoRepository instituicaoRepository;

    public LoginController(UsuarioRepository usuarioRepository,
                           UsuarioInstituicaoRepository usuarioInstituicaoRepository,
                           InstituicaoRepository instituicaoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioInstituicaoRepository = usuarioInstituicaoRepository;
        this.instituicaoRepository = instituicaoRepository;
    }

    @GetMapping("/")
    public String loginForm(Model model) {
        List<Instituicao> instituicoes = instituicaoRepository.findAll();
        model.addAttribute("instituicoes", instituicoes);
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String codUsuario,
                        @RequestParam String senha,
                        @RequestParam Long idInstituicao,
                        Model model) {
        Optional<Usuario> optionalUsuario = usuarioRepository.findByCodUsuarioAndSenha(codUsuario, senha);
        if (optionalUsuario.isPresent()) {
            Usuario usuario = optionalUsuario.get();
            Optional<Instituicao> inst = usuarioInstituicaoRepository
                .findByUsuarioIdAndInstituicaoId(usuario.getId(), idInstituicao)
                .map(ui -> ui.getInstituicao());

            inst.ifPresent(i -> model.addAttribute("nomeInstituicao", i.getNomeInstituicao()));

            return switch (usuario.getNivelAcessoUsuario()) {
                case 1 -> "participante-form";
                case 2 -> "autor-form";
                case 5 -> "administrador-form";
                case 9 -> "superusuario-form";
                default -> "login";
            };
        } else {
            model.addAttribute("erro", "Usuário ou senha inválidos.");
            return "login";
          }
        }
        @GetMapping("/cadastro")
        public String showCadastroForm() {
            return "cadastro-form";
    }
}
