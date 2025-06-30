package com.agendademais.controllers;

import com.agendademais.entities.Usuario;
import com.agendademais.repositories.UsuarioRepository;
import com.agendademais.repositories.InstituicaoRepository;
import jakarta.servlet.http.HttpSession;
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
    private InstituicaoRepository instituicaoRepository;

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("instituicoes", instituicaoRepository.findAll());
        return "login";
    }

    @PostMapping("/login")
    public String processarLogin(@RequestParam String codUsuario,
                                  @RequestParam String senha,
                                  @RequestParam Long instituicao,
                                  Model model) {
    	
        Optional<Usuario> optUsuario = usuarioRepository.findByCodUsuario(codUsuario);

        if (optUsuario.isPresent()) {
            Usuario usuario = optUsuario.get();
            
            if (usuario.getSenha().equals(senha)) {
                // Redirecionamento por nível de acesso
	            switch (usuario.getNivelAcessoUsuario()) {
	                case 1: return "redirect:/participante-form";
	                case 2: return "redirect:/autor-form";
	                case 5: return "redirect:/administrador-form";
	                case 9: return "redirect:/superusuario-form";
	                default: return "redirect:/login";
	            }    
            } else {
                // Senha incorreta
	            model.addAttribute("mensagemErro", "Senha inválida.");
	            model.addAttribute("codUsuario", codUsuario);
	            model.addAttribute("instituicaoSelecionada", instituicao);
	            model.addAttribute("instituicoes", instituicaoRepository.findAll());
	            return "login";
        
            }
        } else {   
            model.addAttribute("mensagemErro", "Usuário não encontrado.");
            model.addAttribute("instituicoes", instituicaoRepository.findAll());
            return "login";
        }    
    }    


    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
