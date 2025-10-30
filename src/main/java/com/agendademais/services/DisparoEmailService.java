package com.agendademais.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import com.agendademais.entities.LogPostagem;
import com.agendademais.entities.OcorrenciaAtividade;
import com.agendademais.repositories.LogPostagemRepository;
import com.agendademais.repositories.OcorrenciaAtividadeRepository;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import java.util.Properties;

@Service
public class DisparoEmailService {
    @Autowired
    private com.agendademais.repositories.InscricaoTipoAtividadeRepository inscricaoTipoAtividadeRepository;

    @Autowired
    private LogPostagemRepository logPostagemRepository;

    @Autowired
    private OcorrenciaAtividadeRepository ocorrenciaAtividadeRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private CryptoService cryptoService;

    @Value("${spring.mail.username:}")
    private String configuredMailUsername;

    @Value("${app.mail.useInstitutionSmtp:false}")
    private boolean useInstitutionSmtp;

    public static class ProgressoDisparo {
        public int total;
        public int enviados;
        public int falhas;
        public List<String> erros = new ArrayList<>();
        public boolean concluido;
        public String fatalError; // New field for fatal errors
    }

    // Simulação de progresso por ocorrenciaId
    private final Map<Long, ProgressoDisparo> progressoMap = new ConcurrentHashMap<>();

    public void iniciarDisparo(Long ocorrenciaId, int totalDestinatarios) {
        ProgressoDisparo progresso = new ProgressoDisparo();
        progresso.total = totalDestinatarios;
        progresso.enviados = 0;
        progresso.falhas = 0;
        progresso.concluido = false;
        progressoMap.put(ocorrenciaId, progresso);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // Mensagem de rodapé para descadastro
                String removerEmailMensagem = "<br><br><hr style='margin:16px 0'>" +
                        "<span style='font-size:12px;color:#888;'>*** Não deseja receber mais nossos emails? acesse o sistema e exclua seu cadastro, ou remova esse tipo de atividade em &quot;Minhas Inscrições em Tipos de Atividades&quot; </span>";
                OcorrenciaAtividade ocorrencia = ocorrenciaAtividadeRepository.findById(ocorrenciaId).orElse(null);
                String assunto = "";
                String conteudoOriginal = ocorrencia != null ? ocorrencia.getDetalheDivulgacao() : "Conteúdo";
                String dataHoraLinha = "";
                if (ocorrencia != null && ocorrencia.getDataOcorrencia() != null
                        && ocorrencia.getHoraInicioOcorrencia() != null && ocorrencia.getHoraFimOcorrencia() != null) {
                    java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yy");
                    String data = dtf.format(ocorrencia.getDataOcorrencia());
                    String horaIni = ocorrencia.getHoraInicioOcorrencia().toString();
                    String horaFim = ocorrencia.getHoraFimOcorrencia().toString();
                    // Assunto igual à pré-visualização
                    String assuntoBase = ocorrencia.getAssuntoDivulgacao() != null ? ocorrencia.getAssuntoDivulgacao()
                            : "";
                    assunto = assuntoBase + " - " + data + " das " + horaIni + " às " + horaFim;
                    // Linha para o corpo do e-mail (opcional, se quiser inserir no texto: Quando:)
                    dataHoraLinha = data + " das " + horaIni + " às " + horaFim + " horas";
                } else {
                    assunto = ocorrencia != null && ocorrencia.getAssuntoDivulgacao() != null
                            ? ocorrencia.getAssuntoDivulgacao()
                            : "Assunto";
                }
                String nomeInstituicao = "";
                String emailInstituicao = "";
                List<String> destinatarios = new ArrayList<>();
                if (ocorrencia != null && ocorrencia.getIdAtividade() != null
                        && ocorrencia.getIdAtividade().getInstituicao() != null) {
                    nomeInstituicao = ocorrencia.getIdAtividade().getInstituicao().getNomeInstituicao();
                    emailInstituicao = ocorrencia.getIdAtividade().getInstituicao().getEmailInstituicao();
                    // Buscar todos os inscritos no tipo de atividade da instituição
                    Long tipoAtividadeId = ocorrencia.getIdAtividade().getTipoAtividade() != null
                            ? ocorrencia.getIdAtividade().getTipoAtividade().getId()
                            : null;
                    Long instituicaoId = ocorrencia.getIdAtividade().getInstituicao().getId();
                    if (tipoAtividadeId != null && instituicaoId != null) {
                        List<com.agendademais.entities.InscricaoTipoAtividade> inscricoes = inscricaoTipoAtividadeRepository
                                .findAll();
                        for (com.agendademais.entities.InscricaoTipoAtividade ita : inscricoes) {
                            if (ita.getTipoAtividade() != null && ita.getTipoAtividade().getId().equals(tipoAtividadeId)
                                    && ita.getInscricao() != null && ita.getInscricao().getIdInstituicao() != null
                                    && ita.getInscricao().getIdInstituicao().getId().equals(instituicaoId)) {
                                com.agendademais.entities.Pessoa pessoa = ita.getInscricao().getPessoa();
                                if (pessoa != null
                                        && pessoa.getEmailPessoa() != null
                                        && !pessoa.getEmailPessoa().isBlank()
                                        && "A".equalsIgnoreCase(pessoa.getSituacaoPessoa())) {
                                    destinatarios.add(pessoa.getEmailPessoa());
                                }
                            }
                        }
                    }
                } else {
                    nomeInstituicao = "Instituição";
                    emailInstituicao = "fclira.fcl@gmail.com"; // fallback para teste
                }
                if (destinatarios.isEmpty()) {
                    // Nenhum destinatário ativo, apenas conclui o progresso
                    progresso.enviados = 0;
                    progresso.concluido = true;
                    progresso.fatalError = "Email não disparado. Nenhuma Pessoa ativa inscrita neste Tipo de Atividade = "
                            + (ocorrencia != null && ocorrencia.getIdAtividade() != null
                                    && ocorrencia.getIdAtividade().getTipoAtividade() != null
                                            ? ocorrencia.getIdAtividade().getTipoAtividade().getId()
                                            : "");
                } else {
                    int i = 0;
                    for (String destinatario : destinatarios) {
                        i++;
                        try {
                            Thread.sleep(500);
                            try {
                                // Decide which mail sender to use: per-institution or default
                                JavaMailSender senderToUse = null;
                                com.agendademais.entities.Instituicao inst = null;
                                if (ocorrencia != null && ocorrencia.getIdAtividade() != null
                                        && ocorrencia.getIdAtividade().getInstituicao() != null) {
                                    inst = ocorrencia.getIdAtividade().getInstituicao();
                                }
                                if (useInstitutionSmtp && inst != null && inst.getSmtpHost() != null
                                        && inst.getSmtpUsername() != null && inst.getSmtpPassword() != null) {
                                    senderToUse = buildSenderForInstitution(inst);
                                } else {
                                    senderToUse = mailSender;
                                }

                                MimeMessage message = senderToUse.createMimeMessage();
                                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                                helper.setTo(destinatario);
                                helper.setSubject(assunto);
                                // Busca pessoa correspondente ao destinatario
                                String nomePessoa = "";
                                com.agendademais.entities.Pessoa pessoaDest = null;
                                if (ocorrencia != null && ocorrencia.getIdAtividade() != null
                                        && ocorrencia.getIdAtividade().getTipoAtividade() != null
                                        && ocorrencia.getIdAtividade().getInstituicao() != null) {
                                    Long tipoAtividadeId = ocorrencia.getIdAtividade().getTipoAtividade().getId();
                                    Long instituicaoId = ocorrencia.getIdAtividade().getInstituicao().getId();
                                    java.util.List<com.agendademais.entities.InscricaoTipoAtividade> inscricoes = inscricaoTipoAtividadeRepository
                                            .findAll();
                                    for (com.agendademais.entities.InscricaoTipoAtividade ita : inscricoes) {
                                        if (ita.getTipoAtividade() != null
                                                && ita.getTipoAtividade().getId().equals(tipoAtividadeId)
                                                && ita.getInscricao() != null
                                                && ita.getInscricao().getIdInstituicao() != null
                                                && ita.getInscricao().getIdInstituicao().getId()
                                                        .equals(instituicaoId)) {
                                            com.agendademais.entities.Pessoa p = ita.getInscricao().getPessoa();
                                            if (p != null && destinatario.equalsIgnoreCase(p.getEmailPessoa())) {
                                                pessoaDest = p;
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (pessoaDest != null && pessoaDest.getNomePessoa() != null) {
                                    nomePessoa = pessoaDest.getNomePessoa();
                                }
                                // Substitui variáveis no corpo do e-mail
                                String conteudo = conteudoOriginal.replace("${nomePessoa}", nomePessoa);
                                conteudo = conteudo.replace("${dataHoraLinha}", dataHoraLinha);
                                conteudo = conteudo.replace("${removerEmailMensagem}", removerEmailMensagem);
                                conteudo = conteudo.replace("${nomeInstituicao}", nomeInstituicao);
                                helper.setText(conteudo, true);
                                // Use authenticated SMTP user as envelope-from when available
                                String envelopeFrom = null;
                                // if using institution sender, prefer its username as envelope
                                if (senderToUse instanceof JavaMailSenderImpl) {
                                    JavaMailSenderImpl jm = (JavaMailSenderImpl) senderToUse;
                                    envelopeFrom = jm.getUsername();
                                }
                                if (envelopeFrom == null || envelopeFrom.isBlank()) {
                                    envelopeFrom = (configuredMailUsername != null && !configuredMailUsername.isBlank())
                                            ? configuredMailUsername
                                            : emailInstituicao;
                                }
                                helper.setFrom(envelopeFrom, nomeInstituicao);
                                // If envelope-from differs from institution email, set Reply-To to institution
                                if (emailInstituicao != null && !emailInstituicao.isBlank()
                                        && !emailInstituicao.equalsIgnoreCase(envelopeFrom)) {
                                    try {
                                        helper.setReplyTo(emailInstituicao);
                                    } catch (Exception rtEx) {
                                        // ignore reply-to failures, not fatal
                                    }
                                }
                                try {
                                    senderToUse.send(message);
                                } catch (Exception sendEx) {
                                    // If we attempted to use institution sender and it failed, fall back to default
                                    if (senderToUse != mailSender && mailSender != null) {
                                        try {
                                            System.err.println(
                                                    "[DisparoEmail] Institution SMTP failed, falling back to default sender: "
                                                            + sendEx.toString());
                                            mailSender.send(message);
                                        } catch (Exception fallbackEx) {
                                            throw fallbackEx; // handled by outer catch
                                        }
                                    } else {
                                        throw sendEx;
                                    }
                                }
                            } catch (Exception ex) {
                                progresso.falhas++;
                                // Capture full stack trace for diagnostics
                                java.io.StringWriter sw = new java.io.StringWriter();
                                java.io.PrintWriter pw = new java.io.PrintWriter(sw);
                                ex.printStackTrace(pw);
                                String stack = sw.toString();
                                String errMsg = destinatario + " falhou: " + ex.toString() + "\n" + stack;
                                progresso.erros.add(errMsg);
                                // Also print to stderr so it's available in console logs
                                System.err.println(
                                        "[DisparoEmail] Erro ao enviar para " + destinatario + ": " + ex.toString());
                                ex.printStackTrace();
                                continue;
                            }
                            progresso.enviados = i;
                            // Simulação de falha a cada 15
                            if (i % 15 == 0) {
                                progresso.falhas++;
                                progresso.erros.add(destinatario + " falhou (simulado)");
                            }
                        } catch (InterruptedException e) {
                            // ignore
                        }
                    }
                    progresso.concluido = true;
                }

                // Salvar log ao final do disparo
                try {
                    OcorrenciaAtividade ocorrenciaLog = ocorrenciaAtividadeRepository.findById(ocorrenciaId)
                            .orElse(null);
                    if (ocorrenciaLog == null) {
                        System.err.println("[LogPostagem] OcorrenciaAtividade não encontrada para ID: " + ocorrenciaId);
                        progresso.fatalError = "OcorrenciaAtividade não encontrada para ID: " + ocorrenciaId;
                    } else {
                        LogPostagem log = new LogPostagem();
                        log.setDataHoraPostagem(LocalDateTime.now());
                        log.setOcorrenciaAtividadeId(ocorrenciaLog.getId());
                        log.setTituloAtividade(
                                ocorrenciaLog.getIdAtividade() != null
                                        && ocorrenciaLog.getIdAtividade().getTituloAtividade() != null
                                                ? ocorrenciaLog.getIdAtividade().getTituloAtividade()
                                                : "");
                        log.setAssuntoDivulgacao(ocorrenciaLog.getAssuntoDivulgacao());
                        log.setTextoDetalheDivulgacao(ocorrenciaLog.getDetalheDivulgacao());
                        // AutorId pode ser null se não houver autor logado
                        log.setAutorId(ocorrenciaLog.getIdAutor() != null ? ocorrenciaLog.getIdAutor().getId() : null);
                        log.setQtEnviados(destinatarios.size());
                        log.setQtFalhas(progresso.falhas);
                        // Mensagem de log
                        if (progresso.falhas > 0) {
                            StringBuilder msg = new StringBuilder();
                            msg.append(progresso.falhas).append(" e-mails rejeitados. ");
                            msg.append("Detalhes: ");
                            for (String erro : progresso.erros) {
                                msg.append(erro).append("; ");
                            }
                            log.setMensagemLogPostagem(msg.toString());
                        } else {
                            log.setMensagemLogPostagem("Nenhuma rejeição registrada.");
                        }
                        try {
                            logPostagemRepository.save(log);
                        } catch (Exception saveEx) {
                            System.err.println("[LogPostagem] Falha ao salvar log para ocorrenciaId=" + ocorrenciaId);
                            saveEx.printStackTrace();
                            progresso.fatalError = "Erro ao salvar log: " + saveEx.getMessage();
                        }
                    }
                } catch (Exception ex) {
                    System.err
                            .println("[LogPostagem] Erro inesperado ao criar/salvar log para ocorrenciaId="
                                    + ocorrenciaId);
                    ex.printStackTrace();
                    progresso.fatalError = "Erro inesperado: " + ex.getMessage();
                }
            }
        });
        thread.start();
    }

    public ProgressoDisparo getProgresso(Long ocorrenciaId) {
        return progressoMap.getOrDefault(ocorrenciaId, null);
    }

    // Helper to build JavaMailSenderImpl for a given institution
    private JavaMailSender buildSenderForInstitution(com.agendademais.entities.Instituicao inst) {
        JavaMailSenderImpl instSender = new JavaMailSenderImpl();
        instSender.setHost(inst.getSmtpHost());
        if (inst.getSmtpPort() != null)
            instSender.setPort(inst.getSmtpPort());
        instSender.setUsername(inst.getSmtpUsername());
        String decrypted = cryptoService.decryptIfNeeded(inst.getSmtpPassword());
        instSender.setPassword(decrypted);
        Properties props = instSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        if (Boolean.TRUE.equals(inst.getSmtpSsl())) {
            props.put("mail.smtp.ssl.enable", "true");
        } else {
            props.put("mail.smtp.starttls.enable", "true");
        }
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        return instSender;
    }
}
