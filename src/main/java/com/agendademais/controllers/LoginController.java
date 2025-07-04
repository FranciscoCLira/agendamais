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
        model.addAttribute("exibirInstituicoes", false); // ou true se quiser que apareça
        return "login";
    }
    
    @PostMapping
    public String processarLogin(
            @RequestParam String codUsuario,
            @RequestParam String senha,
            @RequestParam(required = false) Long instituicao,
            Model model,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        Optional<Usuario> usuarioOpt = usuarioRepository.findByCodUsuario(codUsuario);

        if (usuarioOpt.isEmpty()) {
            model.addAttribute("mensagemErro", "Usuário não encontrado.");
            model.addAttribute("codUsuario", codUsuario);
            return "login";
        }

        Usuario usuario = usuarioOpt.get();

        if (!usuario.getSenha().equals(senha)) {
            model.addAttribute("mensagemErro", "Senha inválida.");
            model.addAttribute("codUsuario", codUsuario);
            return "login";
        }

        boolean temVinculos = usuario.getPessoa() != null &&
                pessoaInstituicaoRepository.existsByPessoaId(usuario.getPessoa().getId());

        if (usuario.getPessoa() != null && (!temVinculos)) {
        	// Salva usuario na sessao com chave "usuarioPendencia"
        	session.setAttribute("usuarioPendencia", usuario);
        	session.setAttribute("senhaPendencia", senha);
        	return "redirect:/cadastro-relacionamentos";
        }

        // Se chegou aqui, carrega Instituições
        model.addAttribute("instituicoes", instituicaoRepository.findAll());
        model.addAttribute("codUsuario", codUsuario);
        model.addAttribute("senha", senha);
        model.addAttribute("exibirInstituicoes", true);
        return "login";
    }

    @PostMapping("/entrar")
    public String confirmarLogin(
            @RequestParam String codUsuario,
            @RequestParam String senha,
            @RequestParam Long instituicao,
            HttpSession session) {

        Usuario usuario = usuarioRepository.findByCodUsuario(codUsuario).orElseThrow();

        session.setAttribute("usuarioLogado", usuario);
        session.setAttribute("instituicaoSelecionada",
                instituicaoRepository.findById(instituicao).orElse(null));

        int nivel = usuario.getNivelAcessoUsuario();
        if (nivel == 1) return "redirect:/participante-form";
        if (nivel == 2) return "redirect:/autor-form";
        if (nivel == 5) return "redirect:/administrador-form";
        if (nivel == 9) return "redirect:/superusuario-form";

        return "redirect:/participante-form";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
