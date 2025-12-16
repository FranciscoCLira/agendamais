package com.agendademais.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import com.agendademais.entities.ConfiguracaoSmtpGlobal;
import com.agendademais.entities.Instituicao;
import com.agendademais.entities.LogPostagem;
import com.agendademais.entities.OcorrenciaAtividade;
import com.agendademais.entities.Regiao;
import com.agendademais.entities.Usuario;
import com.agendademais.entities.UsuarioInstituicao;
import com.agendademais.repositories.ConfiguracaoSmtpGlobalRepository;
import com.agendademais.repositories.LogPostagemRepository;
import com.agendademais.repositories.OcorrenciaAtividadeRepository;
import com.agendademais.repositories.UsuarioInstituicaoRepository;
import com.agendademais.service.RegiaoService;
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
    private com.agendademais.repositories.InstituicaoRepository instituicaoRepository;

    @Autowired
    private ConfiguracaoSmtpGlobalRepository configuracaoSmtpGlobalRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private CryptoService cryptoService;

    @Autowired
    private com.agendademais.service.EmailRodapeService emailRodapeService;

    @Autowired
    private RegiaoService regiaoService;

    @Autowired
    private UsuarioInstituicaoRepository usuarioInstituicaoRepository;

    @Value("${spring.mail.username:}")
    private String configuredMailUsername;

    @Value("${app.mail.useInstitutionSmtp:false}")
    private boolean useInstitutionSmtp;

    @Value("${app.url:http://localhost:8080}")
    private String appUrl;

    public static class ProgressoDisparo {
        public int total;
        public int enviados;
        public int falhas;
        public List<String> erros = new ArrayList<>();
        public boolean concluido;
        public String fatalError; // New field for fatal errors
    }

    /**
     * Coleta a lista de destinatários aplicando as mesmas regras usadas no envio:
     * - Apenas vínculos da instituição da ocorrência
     * - Usuários não bloqueados (situacaoUsuario != B)
     * - Respeita filtro de região (se informado)
     */
    private List<String> coletarDestinatarios(OcorrenciaAtividade ocorrencia, Long regiaoId) {
        List<String> destinatarios = new ArrayList<>();

        if (ocorrencia == null || ocorrencia.getIdAtividade() == null
                || ocorrencia.getIdAtividade().getInstituicao() == null) {
            return destinatarios;
        }

        Long tipoAtividadeId = ocorrencia.getIdAtividade().getTipoAtividade() != null
                ? ocorrencia.getIdAtividade().getTipoAtividade().getId()
                : null;
        Long instituicaoId = ocorrencia.getIdAtividade().getInstituicao().getId();

        if (tipoAtividadeId == null || instituicaoId == null) {
            return destinatarios;
        }

        // Resolve a região uma única vez; se inválida, retorna lista vazia
        Regiao regiaoFiltro = null;
        if (regiaoId != null && regiaoId > 0) {
            try {
                regiaoFiltro = regiaoService.obterPorId(regiaoId);
            } catch (IllegalArgumentException e) {
                System.out.println("[DisparoEmail] Região informada não encontrada: id=" + regiaoId);
                return destinatarios;
            }
        }

        List<com.agendademais.entities.InscricaoTipoAtividade> inscricoes = inscricaoTipoAtividadeRepository
                .findAll();

        int skippedPessoa = 0;
        int skippedUsuario = 0;
        int skippedRegiao = 0;

        for (com.agendademais.entities.InscricaoTipoAtividade ita : inscricoes) {
            if (ita.getTipoAtividade() == null || ita.getInscricao() == null
                    || ita.getInscricao().getIdInstituicao() == null) {
                continue;
            }

            boolean mesmoTipo = ita.getTipoAtividade().getId().equals(tipoAtividadeId);
            boolean mesmaInstituicao = ita.getInscricao().getIdInstituicao().getId().equals(instituicaoId);
            if (!mesmoTipo || !mesmaInstituicao) {
                continue;
            }

            com.agendademais.entities.Pessoa pessoa = ita.getInscricao().getPessoa();
            // Pessoa precisa estar ativa e com email válido
            if (pessoa == null || pessoa.getEmailPessoa() == null || pessoa.getEmailPessoa().isBlank()
                    || pessoa.getSituacaoPessoa() == null
                    || !"A".equalsIgnoreCase(pessoa.getSituacaoPessoa())) {
                skippedPessoa++;
                continue;
            }

            List<UsuarioInstituicao> vinculos = usuarioInstituicaoRepository
                    .findByInstituicaoAndUsuario_Pessoa(ita.getInscricao().getIdInstituicao(), pessoa);
            if (vinculos.isEmpty()) {
                continue; // Sem vínculo UsuarioInstituicao, ignora
            }

            Usuario usuario = vinculos.get(0).getUsuario();
            if (usuario == null) {
                continue; // Sem usuário, ignora
            }

            String situacaoUsuario = usuario.getSituacaoUsuario();
            if (!"A".equalsIgnoreCase(situacaoUsuario) && !"P".equalsIgnoreCase(situacaoUsuario)) {
                skippedUsuario++;
                continue; // Apenas A ou P
            }

            if (regiaoFiltro != null) {
                if (!regiaoService.pessoaPertenceRegiao(pessoa.getCidade(), regiaoFiltro)) {
                    skippedRegiao++;
                    continue; // Fora da região filtrada
                }
            }

            destinatarios.add(pessoa.getEmailPessoa());
        }

        System.out.println("[DisparoEmail] coletarDestinatarios regiaoId=" + regiaoId
                + " -> total=" + destinatarios.size()
                + " (skippedPessoa=" + skippedPessoa
                + ", skippedUsuario=" + skippedUsuario
                + ", skippedRegiao=" + skippedRegiao + ")");

        return destinatarios;
    }

    /**
     * Conta destinatários aplicando todas as regras de envio.
     */
    public long contarDestinatarios(OcorrenciaAtividade ocorrencia, Long regiaoId) {
        return coletarDestinatarios(ocorrencia, regiaoId).size();
    }

    // Simulação de progresso por ocorrenciaId
    private final Map<Long, ProgressoDisparo> progressoMap = new ConcurrentHashMap<>();

    /**
     * Inicia o disparo de email para uma ocorrência com suporte a filtro de região.
     * 
     * @param ocorrenciaId       ID da ocorrência
     * @param totalDestinatarios Número total de destinatários
     * @param regiaoId           ID da região (opcional, null para disparar para
     *                           toda a instituição)
     */
    public void iniciarDisparo(Long ocorrenciaId, int totalDestinatarios, Long regiaoId) {
        ProgressoDisparo progresso = new ProgressoDisparo();
        progresso.total = totalDestinatarios;
        progresso.enviados = 0;
        progresso.falhas = 0;
        progresso.concluido = false;
        progressoMap.put(ocorrenciaId, progresso);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                dispararEmailsInterno(ocorrenciaId, regiaoId);
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Sobrecarga para compatibilidade com código antigo (sem regiaoId).
     */
    public void iniciarDisparo(Long ocorrenciaId, int totalDestinatarios) {
        iniciarDisparo(ocorrenciaId, totalDestinatarios, null);
    }

    /**
     * Interno: realiza o disparo efetivo de emails.
     */
    private void dispararEmailsInterno(Long ocorrenciaId, Long regiaoId) {
        ProgressoDisparo progresso = progressoMap.get(ocorrenciaId);
        if (progresso == null) {
            return; // Não deve acontecer
        }

        OcorrenciaAtividade ocorrencia = ocorrenciaAtividadeRepository.findById(ocorrenciaId).orElse(null);

        // Obter a instituição para passar ao serviço de rodapé
        Instituicao instituicao = null;
        if (ocorrencia != null && ocorrencia.getIdAtividade() != null) {
            instituicao = ocorrencia.getIdAtividade().getInstituicao();
        }

        // Mensagem de rodapé para descadastro (baseada no modo de envio da instituição)
        String removerEmailMensagem = emailRodapeService.gerarMensagemRodape(instituicao);

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
        List<String> destinatarios = coletarDestinatarios(ocorrencia, regiaoId);
        progresso.total = destinatarios.size();

        if (ocorrencia != null && ocorrencia.getIdAtividade() != null
                && ocorrencia.getIdAtividade().getInstituicao() != null) {
            nomeInstituicao = ocorrencia.getIdAtividade().getInstituicao().getNomeInstituicao();
            emailInstituicao = ocorrencia.getIdAtividade().getInstituicao().getEmailInstituicao();
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
                            // Recarrega instituição do banco para garantir campos SMTP atualizados
                            Long instituicaoId = ocorrencia.getIdAtividade().getInstituicao().getId();
                            inst = instituicaoRepository.findById(instituicaoId).orElse(null);

                            // DEBUG: Log configuração SMTP
                            if (inst != null) {
                                System.out.println("[DisparoEmail] Instituição ID=" + inst.getId() + " - "
                                        + inst.getNomeInstituicao());
                                System.out.println("[DisparoEmail] SMTP Host: " + inst.getSmtpHost());
                                System.out.println("[DisparoEmail] SMTP Username: " + inst.getSmtpUsername());
                                System.out.println("[DisparoEmail] SMTP Password exists: "
                                        + (inst.getSmtpPassword() != null
                                                && !inst.getSmtpPassword().isBlank()));
                                System.out.println("[DisparoEmail] useInstitutionSmtp: " + useInstitutionSmtp);
                            }
                        }
                        // Valida se SMTP está completamente configurado (não-null e não-blank)
                        if (useInstitutionSmtp && inst != null
                                && inst.getSmtpHost() != null && !inst.getSmtpHost().isBlank()
                                && inst.getSmtpUsername() != null && !inst.getSmtpUsername().isBlank()
                                && inst.getSmtpPassword() != null && !inst.getSmtpPassword().isBlank()) {
                            // 1ª Prioridade: SMTP Institucional
                            System.out.println(
                                    "[DisparoEmail] ✓ Usando SMTP da instituição: " + inst.getSmtpUsername());
                            try {
                                senderToUse = buildSenderForInstitution(inst);
                            } catch (Exception e) {
                                System.err.println("[DisparoEmail] ERRO ao construir SMTP institucional: "
                                        + e.getMessage());
                                String diagnostico = "Erro ao construir SMTP institucional: " + e.getMessage();
                                progresso.fatalError = diagnostico;
                                throw new RuntimeException("SMTP configurado falhou: " + diagnostico);
                            }
                        } else {
                            // 2ª Prioridade: SMTP Global do banco
                            System.out.println(
                                    "[DisparoEmail] ✗ SMTP institucional não configurado, tentando SMTP global...");
                            Optional<ConfiguracaoSmtpGlobal> configGlobalOpt = configuracaoSmtpGlobalRepository
                                    .findFirstByAtivoTrueOrderByDataCriacaoDesc();

                            if (configGlobalOpt.isPresent()) {
                                ConfiguracaoSmtpGlobal configGlobal = configGlobalOpt.get();
                                System.out.println("[DisparoEmail] ✓ Usando SMTP global (banco): "
                                        + configGlobal.getSmtpUsername());
                                try {
                                    senderToUse = buildSenderForGlobal(configGlobal);
                                } catch (Exception e) {
                                    System.err.println(
                                            "[DisparoEmail] ERRO ao construir SMTP global: " + e.getMessage());
                                    String diagnostico = "Erro ao construir SMTP global: " + e.getMessage();
                                    progresso.fatalError = diagnostico;
                                    throw new RuntimeException("SMTP configurado falhou: " + diagnostico);
                                }
                            } else {
                                // 3ª Prioridade: SMTP das properties (padrão)
                                System.out.println("[DisparoEmail] ⚠ Usando SMTP padrão (properties)");
                                senderToUse = mailSender;
                            }
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
                        String conteudo = conteudoOriginal
                                .replace("{{nomePessoa}}", nomePessoa)
                                .replace("${nomePessoa}", nomePessoa)
                                .replace("{{dataHoraLinha}}", dataHoraLinha)
                                .replace("${dataHoraLinha}", dataHoraLinha)
                                .replace("{{removerEmailMensagem}}", removerEmailMensagem)
                                .replace("${removerEmailMensagem}", removerEmailMensagem)
                                .replace("{{nomeInstituicao}}", nomeInstituicao)
                                .replace("${nomeInstituicao}", nomeInstituicao);
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
                            System.out.println(
                                    "[DisparoEmail] ✓ Email enviado com sucesso para: " + destinatario);
                        } catch (Exception sendEx) {
                            // NOVA REGRA: Para postagens, NÃO usar fallback - interromper com erro
                            // Se SMTP institucional foi configurado e falhou, mostrar erro ao usuário
                            System.err.println(
                                    "[DisparoEmail] ✗ SMTP institucional FALHOU! Erro: " + sendEx.getMessage());
                            System.err.println("[DisparoEmail] Causa raiz: "
                                    + (sendEx.getCause() != null ? sendEx.getCause().getMessage() : "N/A"));

                            // Verifica se é erro de autenticação
                            String errorMsg = sendEx.getMessage() != null ? sendEx.getMessage().toLowerCase()
                                    : "";
                            String causeMsg = sendEx.getCause() != null
                                    && sendEx.getCause().getMessage() != null
                                            ? sendEx.getCause().getMessage().toLowerCase()
                                            : "";

                            String diagnostico = "";
                            if (errorMsg.contains("authentication") &&
                                    (causeMsg.contains("535 5.7.139")
                                            || causeMsg.contains("basic authentication is disabled"))) {
                                diagnostico = "Autenticação básica desabilitada. Contas Outlook.com/Hotmail.com pessoais não permitem mais SMTP com senha. "
                                        +
                                        "Soluções: 1) Usar domínio corporativo M365, 2) Habilitar OAuth2, 3) Usar Gmail com App Password";
                            } else if (errorMsg.contains("authentication")) {
                                diagnostico = "Falha de autenticação. Verifique usuário e senha SMTP.";
                            } else if (errorMsg.contains("connect") || errorMsg.contains("timeout")) {
                                diagnostico = "Falha de conexão. Verifique host, porta e firewall.";
                            } else {
                                diagnostico = sendEx.getMessage();
                            }

                            sendEx.printStackTrace();

                            // NÃO usar fallback - lançar exceção para interromper disparo
                            throw new RuntimeException("SMTP configurado falhou: " + diagnostico, sendEx);
                        }
                    } catch (Exception ex) {
                        // Verifica se é erro fatal de SMTP configurado
                        if (ex.getMessage() != null && ex.getMessage().contains("SMTP configurado falhou")) {
                            // Erro fatal - interrompe disparo completamente
                            progresso.fatalError = ex.getMessage();
                            progresso.concluido = true;
                            System.err.println(
                                    "[DisparoEmail] ✗✗✗ ERRO FATAL - Disparo interrompido: " + ex.getMessage());
                            break; // Sai do loop de destinatários
                        }

                        // Outros erros (por destinatário) - continua tentando
                        progresso.falhas++;
                        java.io.StringWriter sw = new java.io.StringWriter();
                        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
                        ex.printStackTrace(pw);
                        String stack = sw.toString();
                        String errMsg = destinatario + " falhou: " + ex.toString() + "\n" + stack;
                        progresso.erros.add(errMsg);
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
                // Salvar código da região se foi filtrada
                if (regiaoId != null && regiaoId > 0) {
                    try {
                        Regiao regiao = regiaoService.obterPorId(regiaoId);
                        if (regiao != null) {
                            log.setCodRegiao(regiao.getCodRegiao());
                        }
                    } catch (Exception e) {
                        System.err.println("[LogPostagem] Erro ao buscar região: " + e.getMessage());
                    }
                }
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

    public ProgressoDisparo getProgresso(Long ocorrenciaId) {
        return progressoMap.getOrDefault(ocorrenciaId, null);
    }

    // Helper to build JavaMailSenderImpl for a given institution
    private JavaMailSender buildSenderForInstitution(com.agendademais.entities.Instituicao inst) {
        System.err.println("[DisparoEmail] Building sender for institution: " + inst.getNomeInstituicao());
        System.err.println("[DisparoEmail] SMTP Host: " + inst.getSmtpHost());
        System.err.println("[DisparoEmail] SMTP Port: " + inst.getSmtpPort());
        System.err.println("[DisparoEmail] SMTP Username: " + inst.getSmtpUsername());
        System.err.println("[DisparoEmail] SMTP Password length: "
                + (inst.getSmtpPassword() != null ? inst.getSmtpPassword().length() : "NULL"));
        System.err.println("[DisparoEmail] SMTP SSL: " + inst.getSmtpSsl());

        JavaMailSenderImpl instSender = new JavaMailSenderImpl();
        instSender.setHost(inst.getSmtpHost());
        if (inst.getSmtpPort() != null)
            instSender.setPort(inst.getSmtpPort());
        instSender.setUsername(inst.getSmtpUsername());
        String decrypted = cryptoService.decryptIfNeeded(inst.getSmtpPassword());
        System.err.println(
                "[DisparoEmail] Decrypted password length: " + (decrypted != null ? decrypted.length() : "NULL"));
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

    private JavaMailSender buildSenderForGlobal(ConfiguracaoSmtpGlobal config) {
        System.err.println("[DisparoEmail] Building sender for SMTP Global (banco)");
        System.err.println("[DisparoEmail] SMTP Host: " + config.getSmtpHost());
        System.err.println("[DisparoEmail] SMTP Port: " + config.getSmtpPort());
        System.err.println("[DisparoEmail] SMTP Username: " + config.getSmtpUsername());

        JavaMailSenderImpl globalSender = new JavaMailSenderImpl();
        globalSender.setHost(config.getSmtpHost());
        globalSender.setPort(config.getSmtpPort() != null ? config.getSmtpPort() : 587);
        globalSender.setUsername(config.getSmtpUsername());

        String decrypted = cryptoService.decryptIfNeeded(config.getSmtpPassword());
        globalSender.setPassword(decrypted);

        Properties props = globalSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");

        return globalSender;
    }
}
