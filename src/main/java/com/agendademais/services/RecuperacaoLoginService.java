package com.agendademais.services;

import com.agendademais.entities.Usuario;
import com.agendademais.repositories.UsuarioRepository;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RecuperacaoLoginService {

    private final UsuarioRepository usuarioRepository;
    private final JavaMailSender mailSender;

    // @Autowired
    public RecuperacaoLoginService(UsuarioRepository usuarioRepository,
                                   JavaMailSender mailSender) {
        this.usuarioRepository = usuarioRepository;
        this.mailSender = mailSender;
    }

    public void enviarLinkRecuperacao(String email) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmailPessoa(email);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            // Cria o corpo do email com link para recuperação
            String assunto = "Recuperação de Acesso - Agenda Mais";
            String mensagem = String.format(
                "Olá %s,\n\n"
              + "Recebemos uma solicitação de recuperação de acesso ao sistema Agenda Mais.\n"
              + "Clique no link abaixo para redefinir sua senha:\n\n"
              + "http://localhost:8080/recuperar-senha?email=%s\n\n"
              + "Se você não fez esta solicitação, ignore este e-mail.",
                usuario.getPessoa().getNomePessoa(),
                usuario.getPessoa().getEmailPessoa()
            );

            // Configura e envia o email
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(usuario.getPessoa().getEmailPessoa());
            mailMessage.setSubject(assunto);
            mailMessage.setText(mensagem);

            mailSender.send(mailMessage);
        }
    }
}
