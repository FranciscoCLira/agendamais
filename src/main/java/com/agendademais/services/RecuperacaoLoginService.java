package com.agendademais.services;

import com.agendademais.entities.ConfiguracaoSmtpGlobal;
import com.agendademais.entities.Instituicao;
import com.agendademais.entities.Pessoa;
import com.agendademais.entities.PessoaInstituicao;
import com.agendademais.entities.Usuario;
import com.agendademais.repositories.ConfiguracaoSmtpGlobalRepository;
import com.agendademais.repositories.PessoaRepository;
import com.agendademais.repositories.PessoaInstituicaoRepository;
import com.agendademais.repositories.UsuarioRepository;
import com.agendademais.services.CryptoService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

@Service
public class RecuperacaoLoginService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private PessoaInstituicaoRepository pessoaInstituicaoRepository;

    @Autowired
    private ConfiguracaoSmtpGlobalRepository configuracaoSmtpGlobalRepository;

    @Autowired
    private CryptoService cryptoService;

    @Value("${app.url:http://localhost:8080}")
    private String appUrl;

    @Value("${spring.mail.username:noreply@agendamais.com}")
    private String configuredMailUsername;

    @Transactional
    public void enviarLinkRecuperacao(String email) throws Exception {
        // 1. Buscar usuário pelo email
        Usuario usuario = usuarioRepository.findAllByPessoaEmailPessoa(email)
                .stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        // 2. Gerar e salvar token ANTES de enviar email
        String token = UUID.randomUUID().toString();
        usuario.setTokenRecuperacao(token);
        usuario.setDataExpiracaoToken(LocalDateTime.now().plusHours(2));
        usuario.setDataUltimaAtualizacao(LocalDate.now());
        usuarioRepository.save(usuario);
        usuarioRepository.flush(); // Força persistência imediata

        // 3. Buscar instituição da pessoa (se existir)
        Instituicao instituicao = buscarInstituicaoPorEmail(email);

        // 4. Preparar remetente do email com prioridade: Institucional → Global (Banco)
        // → Padrão (Properties)
        JavaMailSender senderToUse = mailSender;

        if (instituicao != null && instituicao.getSmtpHost() != null) {
            // 1ª Prioridade: SMTP da instituição
            try {
                senderToUse = criarMailSenderInstituicao(instituicao);
            } catch (Exception e) {
                // Fallback silencioso para SMTP Global se falhar
                System.err.println("Erro ao criar SMTP institucional, usando global: " + e.getMessage());
            }
        }

        // 2ª Prioridade: SMTP Global do banco (se SMTP institucional não existir ou
        // falhar)
        if (senderToUse == mailSender) {
            Optional<ConfiguracaoSmtpGlobal> configGlobalOpt = configuracaoSmtpGlobalRepository
                    .findFirstByAtivoTrueOrderByDataCriacaoDesc();

            if (configGlobalOpt.isPresent()) {
                ConfiguracaoSmtpGlobal configGlobal = configGlobalOpt.get();
                try {
                    senderToUse = criarMailSenderGlobal(configGlobal);
                } catch (Exception e) {
                    // Fallback silencioso para properties se falhar
                    System.err.println("Erro ao criar SMTP global, usando properties: " + e.getMessage());
                }
            }
        }
        // 3ª Prioridade: SMTP das properties (mailSender injetado) - já está em
        // senderToUse se nenhum anterior funcionou

        // Determinar email remetente: usar username do SMTP autenticado
        String emailRemetente = null;
        if (senderToUse instanceof JavaMailSenderImpl) {
            JavaMailSenderImpl jm = (JavaMailSenderImpl) senderToUse;
            emailRemetente = jm.getUsername();
        }
        if (emailRemetente == null || emailRemetente.isBlank()) {
            // Fallback: email da instituição ou properties
            emailRemetente = (instituicao != null && instituicao.getEmailInstituicao() != null)
                    ? instituicao.getEmailInstituicao()
                    : configuredMailUsername;
        }

        // 5. Montar link e mensagem
        String link = appUrl + "/recuperar-senha-por-token?token=" + token;

        String mensagemHtml = "<html><body style='font-family:Segoe UI, sans-serif; color:#333;'>"
                + "<div style='border:1px solid #ddd; padding:20px; border-radius:6px;'>"
                + "<h2 style='color:#4A148C;'>Recuperação de Senha - AgendaMais</h2>"
                + "<p>Olá!</p>"
                + "<p>Recebemos uma solicitação para redefinir sua senha.</p>"
                + "<p>Seu código de usuário é: <strong>" + usuario.getUsername() + "</strong></p>"
                + "<p>Acesse o link abaixo para continuar:</p>"
                + "<p><a href='" + link + "' style='color:#4A148C; text-decoration:none; font-weight:bold;'>"
                + link + "</a></p>"
                + "<p style='color:#999; font-size:12px;'>Esse link expira em 2 horas.</p>"
                + "<p>Se você não solicitou, ignore esta mensagem.</p>"
                + "<br>"
                + "<p>Atenciosamente,<br>Equipe Agenda Mais</p>"
                + "</div>"
                + "</body></html>";

        // 6. Enviar email
        MimeMessage mimeMessage = senderToUse.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setFrom(emailRemetente);
        helper.setTo(email);
        helper.setSubject("Recuperação de Senha - AgendaMais");
        helper.setText(mensagemHtml, true);

        senderToUse.send(mimeMessage);
    }

    /**
     * Busca a instituição associada à pessoa pelo email
     * Retorna a primeira instituição COM SMTP CONFIGURADO encontrada via
     * PessoaInstituicao
     * Se não encontrar instituição com SMTP, retorna null para usar SMTP padrão
     */
    private Instituicao buscarInstituicaoPorEmail(String email) {
        Optional<Pessoa> pessoaOpt = pessoaRepository.findByEmailPessoa(email);

        if (pessoaOpt.isPresent()) {
            Pessoa pessoa = pessoaOpt.get();

            // Buscar instituições da pessoa via PessoaInstituicao
            // Filtra APENAS aquelas que têm SMTP COMPLETAMENTE configurado
            Optional<Instituicao> instituicaoComSmtp = pessoaInstituicaoRepository
                    .findByPessoaId(pessoa.getId())
                    .stream()
                    .map(PessoaInstituicao::getInstituicao)
                    .filter(inst -> inst != null
                            && inst.getSmtpHost() != null && !inst.getSmtpHost().isBlank()
                            && inst.getSmtpUsername() != null && !inst.getSmtpUsername().isBlank()
                            && inst.getSmtpPassword() != null && !inst.getSmtpPassword().isBlank())
                    .findFirst();

            if (instituicaoComSmtp.isPresent()) {
                return instituicaoComSmtp.get();
            }
        }

        return null; // Usa SMTP padrão se não encontrar instituição com SMTP configurado
    }

    /**
     * Cria um JavaMailSender customizado com configurações SMTP da instituição
     */
    private JavaMailSender criarMailSenderInstituicao(Instituicao instituicao) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost(instituicao.getSmtpHost());
        mailSender.setPort(instituicao.getSmtpPort() != null ? instituicao.getSmtpPort() : 587);
        mailSender.setUsername(instituicao.getSmtpUsername());

        // Descriptografa senha se necessário (remove ENC(...))
        String senhaDescriptografada = cryptoService.decryptIfNeeded(instituicao.getSmtpPassword());
        mailSender.setPassword(senhaDescriptografada);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");

        // SSL (465) ou STARTTLS (587)
        if (instituicao.getSmtpSsl() != null && instituicao.getSmtpSsl()) {
            props.put("mail.smtp.ssl.enable", "true");
        } else {
            props.put("mail.smtp.starttls.enable", "true");
        }

        props.put("mail.debug", "false");

        return mailSender;
    }

    /**
     * Cria um JavaMailSender com configurações SMTP globais do banco
     */
    private JavaMailSender criarMailSenderGlobal(ConfiguracaoSmtpGlobal config) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost(config.getSmtpHost());
        mailSender.setPort(config.getSmtpPort() != null ? config.getSmtpPort() : 587);
        mailSender.setUsername(config.getSmtpUsername());

        // Descriptografa senha se necessário
        String senhaDescriptografada = cryptoService.decryptIfNeeded(config.getSmtpPassword());
        mailSender.setPassword(senhaDescriptografada);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "false");

        return mailSender;
    }

}
