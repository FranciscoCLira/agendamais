package com.agendademais.controllers;

import com.agendademais.entities.Usuario;
import com.agendademais.repositories.InstituicaoRepository;
import com.agendademais.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class RecuperacaoLoginController {

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private InstituicaoRepository instituicaoRepository;
    
    @Autowired
    private JavaMailSender mailSender;


    @PostMapping("/recuperar-login")
    public String recuperarLogin(@RequestParam String email, Model model) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmailPessoa(email);

        if (usuarioOpt.isPresent()) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Recuperação de Usuário/Senha");
            message.setText("Olá!\n\nSeu usuário é: " + usuarioOpt.get().getCodUsuario());
            mailSender.send(message);

            model.addAttribute("mensagemRecuperacao",
                    "Enviamos uma mensagem para o e-mail " + email);
        } else {
            model.addAttribute("mensagemRecuperacao",
                    "Email não cadastrado.");
        }
        
        model.addAttribute("instituicoes", instituicaoRepository.findAll());
        return "login";
    }


}
