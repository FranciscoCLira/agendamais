package com.agendademais.controllers;

import com.agendademais.entities.Usuario;
import com.agendademais.repositories.UsuarioRepository;

import jakarta.servlet.http.HttpSession;

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
    public String exibirFormulario(
            @RequestParam(required = false) String origem,
            Model model,
            HttpSession session) {

        // Salva a origem do cadastro na sessão
        if (origem != null) {
            session.setAttribute("origemCadastro", origem);
        }

        // Passa a origem para o template também
        String origemAtual = (String) session.getAttribute("origemCadastro");
        model.addAttribute("origem", origemAtual);

        if (!model.containsAttribute("usuario")) {
            model.addAttribute("usuario", new Usuario());
        }
        return "cadastro-usuario";
    }

    @PostMapping
    public String cadastrarUsuario(
            @ModelAttribute Usuario usuario,
            @RequestParam String confirmPassword, // Caso queira usar no form
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        // Validar senha
        if (usuario.getPassword() == null || usuario.getPassword().length() < 6) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Senha deve ter no mínimo 6 caracteres.");
            redirectAttributes.addFlashAttribute("usuario", usuario);
            return "redirect:/cadastro-usuario";
        }

        // Checa se usuário já existe
        Optional<Usuario> existente = usuarioRepository.findByUsername(usuario.getUsername());
        if (existente.isPresent()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Usuário já existente. Escolha outro.");
            redirectAttributes.addFlashAttribute("usuario", usuario);
            return "redirect:/cadastro-usuario";
        }

        if (usuario.getUsername().length() < 6 || usuario.getUsername().length() > 25) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "O Código do Usuário deve ter entre 6 e 25 caracteres.");
            redirectAttributes.addFlashAttribute("usuario", usuario);
            return "redirect:/cadastro-usuario";
        }

        // VALIDAR SENHA
        if (!isSenhaSegura(usuario.getPassword())) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "A senha deve ter no mínimo 6 caracteres e conter letras, números ou símbolos.");
            redirectAttributes.addFlashAttribute("usuario", usuario);
            return "redirect:/cadastro-usuario";
        }

        if (!usuario.getPassword().equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("mensagemErro", "As senhas não coincidem.");
            redirectAttributes.addFlashAttribute("usuario", usuario);
            return "redirect:/cadastro-usuario";
        }

        // Aqui você pode validar/criptografar a senha se desejar!
        // usuario.setSenha(criptografada...)

        // Ainda não salva em banco: primeiro finaliza o cadastro da pessoa
        session.setAttribute("usuarioCadastro", usuario);

        // Opcional: mensagem de instrução
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Usuário criado! Complete seus dados pessoais.");
        return "redirect:/cadastro-pessoa";
    }

    private boolean isSenhaSegura(String password) {
        if (password == null || password.length() < 6)
            return false;
        return password.matches(".*[a-zA-Z].*") && (password.matches(".*\\d.*") || password.matches(".*\\W.*"));
    }
}
