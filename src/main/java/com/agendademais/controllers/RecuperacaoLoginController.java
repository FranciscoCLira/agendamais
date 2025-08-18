
package com.agendademais.controllers;

import com.agendademais.entities.Usuario;
import com.agendademais.repositories.InstituicaoRepository;
import com.agendademais.repositories.UsuarioRepository;

import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
            Usuario usuario = usuarioOpt.get();

            try {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

                helper.setTo(email);
                helper.setSubject("Recuperação de Usuário/Senha - Agenda Mais");
                helper.setText(
                        "<html><body style='font-family:Segoe UI, sans-serif; color:#333;'>"
                                + "<div style='border:1px solid #ddd; padding:20px; border-radius:6px;'>"
                                + "<h2 style='color:#4A148C;'>Agenda Mais</h2>"
                                + "<p>Olá <strong>" + usuario.getPessoa().getNomePessoa() + "</strong>,</p>"
                                + "<p>Seu <strong>usuário</strong> é:</p>"
                                + "<p style='font-size:18px; color:#4A148C;'><strong>" + usuario.getUsername()
                                + "</strong></p>"
                                + "<p>Por segurança, sua senha não é enviada.</p>"
                                + "<p>Para redefinir sua senha, clique no link abaixo:</p>"
                                + "<p><a href='http://localhost:8080/recuperar-senha' style='color:#4A148C;'>Recuperar Senha</a></p>"
                                + "<br>"
                                + "<p>Atenciosamente,<br>Equipe Agenda Mais</p>"
                                + "</div>"
                                + "</body></html>",
                        true);

                mailSender.send(mimeMessage);

                model.addAttribute("mensagemRecuperacao",
                        "Enviamos uma mensagem para o e-mail " + email + ".");

            } catch (Exception e) {
                model.addAttribute("mensagemRecuperacao",
                        "Ocorreu um erro ao enviar o e-mail. Tente novamente mais tarde.");
                e.printStackTrace(); // Loga o erro no console
            }

        } else {
            model.addAttribute("mensagemRecuperacao",
                    "Email não cadastrado.");
        }

        model.addAttribute("instituicoes", instituicaoRepository.findAll());
        return "login";
    }

}
