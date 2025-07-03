package com.agendademais.controllers;

import com.agendademais.entities.Usuario;
import com.agendademais.repositories.InstituicaoRepository;
import com.agendademais.repositories.PessoaInstituicaoRepository;
import com.agendademais.repositories.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/login")
public class LoginController {

    private final UsuarioRepository usuarioRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final PessoaInstituicaoRepository pessoaInstituicaoRepository;

    public LoginController(
            UsuarioRepository usuarioRepository,
            InstituicaoRepository instituicaoRepository,
            PessoaInstituicaoRepository pessoaInstituicaoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.instituicaoRepository = instituicaoRepository;
        this.pessoaInstituicaoRepository = pessoaInstituicaoRepository;
    }

    @GetMapping
    public String loginForm(Model model) {
        model.addAttribute("instituicoes", instituicaoRepository.findAll());
        return "login";
    }

    @PostMapping
    public String processarLogin(
            @RequestParam String codUsuario,
            @RequestParam String senha,
            @RequestParam(required = false) Long instituicao,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        Optional<Usuario> usuarioOpt = usuarioRepository.findByCodUsuario(codUsuario);

        if (usuarioOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Usuário não encontrado.");
            return "redirect:/login";
        }

        Usuario usuario = usuarioOpt.get();

        if (!usuario.getSenha().equals(senha)) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Senha inválida.");
            redirectAttributes.addFlashAttribute("codUsuario", codUsuario);
            return "redirect:/login";
        }

        // Verifica se há vínculos
        boolean temVinculos = usuario.getPessoa() != null &&
                pessoaInstituicaoRepository.existsByPessoaId(usuario.getPessoa().getId());

        if (!temVinculos) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Seu cadastro está pendente. Conclua seus vínculos antes de acessar o sistema.");
            redirectAttributes.addFlashAttribute("codUsuario", codUsuario);
            return "redirect:/cadastro-relacionamentos?codUsuario=" + codUsuario;
        }

        // Salva na sessão
        session.setAttribute("usuarioLogado", usuario);
        session.setAttribute("instituicaoSelecionada", instituicao);

        // Redirecionamento por nível
        int nivel = usuario.getNivelAcessoUsuario();
        if (nivel == 1) return "redirect:/participante-form";
        if (nivel == 2) return "redirect:/autor-form";
        if (nivel == 5) return "redirect:/administrador-form";
        if (nivel == 9) return "redirect:/superusuario-form";

        // Default: Participante
        return "redirect:/participante-form";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
