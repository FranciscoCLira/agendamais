package com.agendademais.services;

import com.agendademais.entities.Usuario;
import com.agendademais.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RecuperacaoLoginService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Async
    public void enviarLinkRecuperacao(String email) {
        Usuario usuario = usuarioRepository.findAllByPessoaEmailPessoa(email)
                .stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        String token = UUID.randomUUID().toString();
        usuario.setTokenRecuperacao(token);
        usuario.setDataExpiracaoToken(LocalDateTime.now().plusHours(2));
        usuario.setDataUltimaAtualizacao(LocalDate.now());
        usuarioRepository.save(usuario);

        String link = "http://localhost:8080/recuperar-senha-por-token?token=" + token;

        String mensagem = "Olá!\n\n"
                + "Recebemos uma solicitação para redefinir sua senha.\n\n"
                + "Seu código de usuário é: " + usuario.getUsername() + "\n\n"
                + "Acesse o link abaixo para continuar:\n" + link + "\n\n"
                + "Esse link expira em 2 horas.\n\n"
                + "Se você não solicitou, ignore esta mensagem.";
        
        SimpleMailMessage emailMsg = new SimpleMailMessage();
        emailMsg.setTo(email);
        emailMsg.setSubject("Recuperação de Senha - AgendaMais");
        emailMsg.setText(mensagem);

        mailSender.send(emailMsg);
    }

}
