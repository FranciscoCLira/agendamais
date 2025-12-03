package com.agendademais.service;

import com.agendademais.entities.*;
import com.agendademais.model.DisparoEmailBatch;
import com.agendademais.model.DisparoEmailBatch.StatusDisparo;
import com.agendademais.model.DisparoEmailBatch.TipoDisparo;
import com.agendademais.repository.DisparoEmailBatchRepository;
import com.agendademais.repositories.UsuarioInstituicaoRepository;
import com.agendademais.repositories.InstituicaoRepository;
import com.agendademais.repositories.ConfiguracaoSmtpGlobalRepository;
import com.agendademais.repositories.LogPostagemRepository;
import com.agendademais.services.CryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service para disparo genérico de emails em lote.
 * Suporta filtros avançados e templates customizáveis.
 */
@Service
public class DisparoEmailGenericoService {

    @Autowired
    private DisparoEmailBatchRepository disparoBatchRepository;

    @Autowired
    private UsuarioInstituicaoRepository usuarioInstituicaoRepository;

    @Autowired
    private InstituicaoRepository instituicaoRepository;

    @Autowired
    private ConfiguracaoSmtpGlobalRepository configuracaoSmtpGlobalRepository;

    @Autowired
    private LogPostagemRepository logPostagemRepository;

    @Autowired
    private CryptoService cryptoService;

    @Autowired
    private JavaMailSender defaultMailSender;

    @Value("${spring.mail.username:noreply@agendamais.com}")
    private String defaultEmailFrom;

    /**
     * Lista destinatários (Pessoas) baseado nos filtros do disparo.
     * Nova lógica: usa UsuarioInstituicao em vez de Inscricao.
     * 
     * REGRAS:
     * - BOAS_VINDAS: Obrigatoriamente situacaoUsuario = 'P' (Pendente)
     * - INFORMATIVO/CAMPANHA: Filtra por situacaoUsuario conforme selecionado no
     * dropdown
     * - Data Cadastro: Compara com pessoa.dataInclusao dentro do intervalo
     * especificado
     */
    public List<Pessoa> listarDestinatarios(DisparoEmailBatch disparo) {
        // Buscar todos os vínculos usuário-instituição da instituição logada
        List<UsuarioInstituicao> vinculos = usuarioInstituicaoRepository
                .findByInstituicaoOrderByNivelAcessoUsuarioInstituicaoAsc(disparo.getInstituicao());

        System.out.println("=== DEBUG DISPARO EMAILS (Nova Lógica) ===");
        System.out.println("Instituição: " + disparo.getInstituicao().getNomeInstituicao());
        System.out.println("Tipo Disparo: " + disparo.getTipoDisparo());
        System.out.println("Total de vínculos UsuarioInstituicao: " + vinculos.size());
        System.out.println("Filtro situação: " + disparo.getFiltroSituacaoUsuario());
        System.out.println("Filtro data início: " + disparo.getFiltroDataInscricaoInicio());
        System.out.println("Filtro data fim: " + disparo.getFiltroDataInscricaoFim());

        List<Pessoa> resultado = vinculos.stream()
                .map(UsuarioInstituicao::getUsuario)
                .filter(usuario -> {
                    if (usuario == null) {
                        System.out.println("  - Usuário NULL ignorado");
                        return false;
                    }

                    Pessoa pessoa = usuario.getPessoa();
                    if (pessoa == null) {
                        System.out.println("  - Usuário " + usuario.getId() + " sem Pessoa vinculada");
                        return false;
                    }

                    // Filtro 1: Email válido (obrigatório)
                    if (pessoa.getEmailPessoa() == null || pessoa.getEmailPessoa().isBlank()) {
                        System.out.println("  - Usuário " + usuario.getUsername() + " ignorado: sem email");
                        return false;
                    }

                    // Filtro 2: Situação do usuário (baseado no tipo de disparo)
                    String situacaoUsuario = usuario.getSituacaoUsuario();
                    String filtroSituacao = disparo.getFiltroSituacaoUsuario();

                    // REGRA ESPECIAL: BOAS_VINDAS obriga situacao = 'P'
                    if (disparo.getTipoDisparo() == TipoDisparo.BOAS_VINDAS) {
                        if (!"P".equalsIgnoreCase(situacaoUsuario)) {
                            System.out.println("  - Usuário " + usuario.getUsername()
                                    + " ignorado: BOAS_VINDAS requer situacao=P, encontrado=" + situacaoUsuario);
                            return false;
                        }
                    } else {
                        // INFORMATIVO ou CAMPANHA: respeita filtro selecionado no dropdown
                        if (filtroSituacao != null && !filtroSituacao.isBlank()) {
                            if (!filtroSituacao.equalsIgnoreCase(situacaoUsuario)) {
                                System.out.println("  - Usuário " + usuario.getUsername() + " ignorado: situacao="
                                        + situacaoUsuario + ", filtro=" + filtroSituacao);
                                return false;
                            }
                        }
                        // Se filtroSituacao estiver vazio/null, aceita TODOS (Todas as situações)
                    }

                    // Filtro 3: Data de inclusão da Pessoa dentro do intervalo (se informado)
                    LocalDate dataInclusao = pessoa.getDataInclusao();
                    if (dataInclusao != null) {
                        // Filtro de data início
                        if (disparo.getFiltroDataInscricaoInicio() != null
                                && dataInclusao.isBefore(disparo.getFiltroDataInscricaoInicio())) {
                            System.out.println("  - Usuário " + usuario.getUsername() + " ignorado: data "
                                    + dataInclusao + " anterior a " + disparo.getFiltroDataInscricaoInicio());
                            return false;
                        }

                        // Filtro de data fim
                        if (disparo.getFiltroDataInscricaoFim() != null
                                && dataInclusao.isAfter(disparo.getFiltroDataInscricaoFim())) {
                            System.out.println("  - Usuário " + usuario.getUsername() + " ignorado: data "
                                    + dataInclusao + " posterior a " + disparo.getFiltroDataInscricaoFim());
                            return false;
                        }
                    }

                    System.out.println("  + Usuário " + usuario.getUsername() + " (" + pessoa.getNomePessoa()
                            + ") INCLUÍDO: situacao=" + situacaoUsuario + ", dataInclusao=" + dataInclusao);
                    return true;
                })
                .map(Usuario::getPessoa)
                .distinct()
                .collect(Collectors.toList());

        System.out.println("Total de destinatários após filtros: " + resultado.size());
        System.out.println("==========================================");

        return resultado;
    }

    /**
     * Cria um novo disparo de email.
     */
    @Transactional
    public DisparoEmailBatch criarDisparo(DisparoEmailBatch disparo) {
        disparo.setDataCriacao(LocalDateTime.now());
        disparo.setStatus(StatusDisparo.PENDENTE);

        // Calcular total de destinatários
        List<Pessoa> destinatarios = listarDestinatarios(disparo);
        disparo.setTotalDestinatarios(destinatarios.size());

        return disparoBatchRepository.save(disparo);
    }

    /**
     * Processa um disparo de email em background.
     */
    @Async
    @Transactional
    public void processarDisparoAsync(Long disparoId) {
        DisparoEmailBatch disparo = disparoBatchRepository.findById(disparoId)
                .orElseThrow(() -> new RuntimeException("Disparo não encontrado: " + disparoId));

        try {
            disparo.setStatus(StatusDisparo.PROCESSANDO);
            disparo.setDataInicioProcessamento(LocalDateTime.now());
            disparoBatchRepository.save(disparo);

            List<Pessoa> destinatarios = listarDestinatarios(disparo);
            JavaMailSender mailSender = obterMailSender(disparo.getInstituicao());

            int enviados = 0;
            int falhados = 0;

            for (Pessoa pessoa : destinatarios) {
                try {
                    String corpoProcessado = processarTemplate(disparo.getCorpoHtml(), pessoa,
                            disparo.getInstituicao());
                    enviarEmail(mailSender, pessoa.getEmailPessoa(), disparo.getAssunto(), corpoProcessado,
                            disparo.getInstituicao());

                    // Registrar no log_postagem
                    registrarLogPostagem(disparo, pessoa, true, null);
                    enviados++;

                } catch (Exception e) {
                    System.err.println("Erro ao enviar email para " + pessoa.getEmailPessoa() + ": " + e.getMessage());
                    registrarLogPostagem(disparo, pessoa, false, e.getMessage());
                    falhados++;
                }

                // Atualizar estatísticas periodicamente
                if ((enviados + falhados) % 10 == 0) {
                    disparo.setEmailsEnviados(enviados);
                    disparo.setEmailsFalhados(falhados);
                    disparoBatchRepository.save(disparo);
                }
            }

            // Finalizar
            disparo.setEmailsEnviados(enviados);
            disparo.setEmailsFalhados(falhados);
            disparo.setStatus(StatusDisparo.CONCLUIDO);
            disparo.setDataFimProcessamento(LocalDateTime.now());
            disparoBatchRepository.save(disparo);

        } catch (Exception e) {
            disparo.setStatus(StatusDisparo.ERRO);
            disparo.setMensagemErro(e.getMessage());
            disparo.setDataFimProcessamento(LocalDateTime.now());
            disparoBatchRepository.save(disparo);
            throw new RuntimeException("Erro ao processar disparo: " + e.getMessage(), e);
        }
    }

    /**
     * Processa template substituindo variáveis.
     */
    public String processarTemplate(String template, Pessoa pessoa, Instituicao instituicao) {
        String resultado = template;

        // Variáveis da pessoa
        resultado = resultado.replace("{{nome}}", pessoa.getNomePessoa() != null ? pessoa.getNomePessoa() : "");
        resultado = resultado.replace("{{username}}", pessoa.getEmailPessoa() != null ? pessoa.getEmailPessoa() : ""); // Usando
                                                                                                                       // email
                                                                                                                       // como
                                                                                                                       // username
        resultado = resultado.replace("{{email}}", pessoa.getEmailPessoa() != null ? pessoa.getEmailPessoa() : "");

        // Variáveis da instituição
        if (instituicao != null) {
            resultado = resultado.replace("{{nomeInstituicao}}",
                    instituicao.getNomeInstituicao() != null ? instituicao.getNomeInstituicao() : "");
            resultado = resultado.replace("{{emailInstituicao}}",
                    instituicao.getEmailInstituicao() != null ? instituicao.getEmailInstituicao() : "");
        }

        // Variáveis de sistema
        resultado = resultado.replace("{{appUrl}}", "http://localhost:8081"); // Porta correta dev-docker
        resultado = resultado.replace("{{dataAtual}}", LocalDateTime.now().toString());

        // Mensagem de rodapé para descadastro
        String removerEmailMensagem = "<br><br><hr style='margin:16px 0'>" +
                "<span style='font-size:12px;color:#888;'>*** Não deseja receber mais nossos emails? acesse o sistema e exclua seu cadastro, ou remova esse tipo de atividade em &quot;Minhas Inscrições em Tipos de Atividades&quot;<br>"
                +
                "Acesse: <a href='http://localhost:8081' style='color:#0066cc;'>http://localhost:8081</a></span>";
        resultado = resultado.replace("{{removerEmailMensagem}}", removerEmailMensagem);

        return resultado;
    }

    /**
     * Obtém JavaMailSender com prioridade: Institucional → Global (DB) →
     * Properties.
     */
    private JavaMailSender obterMailSender(Instituicao instituicao) {
        // 1º Tentar SMTP institucional
        if (instituicao != null &&
                instituicao.getSmtpHost() != null && !instituicao.getSmtpHost().isBlank() &&
                instituicao.getSmtpUsername() != null && !instituicao.getSmtpUsername().isBlank()) {

            try {
                return buildSenderForInstitution(instituicao);
            } catch (Exception e) {
                System.err.println("Erro ao criar sender institucional, tentando global: " + e.getMessage());
            }
        }

        // 2º Tentar SMTP global do banco
        Optional<ConfiguracaoSmtpGlobal> configGlobalOpt = configuracaoSmtpGlobalRepository
                .findFirstByAtivoTrueOrderByDataCriacaoDesc();
        if (configGlobalOpt.isPresent()) {
            try {
                return buildSenderForGlobal(configGlobalOpt.get());
            } catch (Exception e) {
                System.err.println("Erro ao criar sender global, usando properties: " + e.getMessage());
            }
        }

        // 3º Fallback para properties
        return defaultMailSender;
    }

    /**
     * Cria JavaMailSender para instituição.
     */
    private JavaMailSender buildSenderForInstitution(Instituicao inst) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(inst.getSmtpHost());
        sender.setPort(inst.getSmtpPort() != null ? inst.getSmtpPort() : 587);
        sender.setUsername(inst.getSmtpUsername());

        String decryptedPassword = cryptoService.decryptIfNeeded(inst.getSmtpPassword());
        sender.setPassword(decryptedPassword);

        Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", inst.getSmtpHost());
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.connectiontimeout", "10000");

        return sender;
    }

    /**
     * Cria JavaMailSender para configuração global.
     */
    private JavaMailSender buildSenderForGlobal(ConfiguracaoSmtpGlobal config) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(config.getSmtpHost());
        sender.setPort(config.getSmtpPort() != null ? config.getSmtpPort() : 587);
        sender.setUsername(config.getSmtpUsername());

        String decryptedPassword = cryptoService.decryptIfNeeded(config.getSmtpPassword());
        sender.setPassword(decryptedPassword);

        Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", config.getSmtpHost());
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.connectiontimeout", "10000");

        return sender;
    }

    /**
     * Envia email usando o username do SMTP autenticado como remetente.
     * Mesma lógica do DisparoEmailService para garantir compatibilidade.
     */
    private void enviarEmail(JavaMailSender mailSender, String destinatario, String assunto, String corpo,
            Instituicao instituicao) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(destinatario);
        helper.setSubject(assunto);
        helper.setText(corpo, true); // HTML

        // Usar o username do SMTP autenticado como envelope-from (remetente)
        String envelopeFrom = null;
        if (mailSender instanceof JavaMailSenderImpl) {
            JavaMailSenderImpl jm = (JavaMailSenderImpl) mailSender;
            envelopeFrom = jm.getUsername();
        }

        // Fallback: email da instituição ou properties
        if (envelopeFrom == null || envelopeFrom.isBlank()) {
            envelopeFrom = (instituicao.getEmailInstituicao() != null && !instituicao.getEmailInstituicao().isBlank())
                    ? instituicao.getEmailInstituicao()
                    : defaultEmailFrom;
        }

        helper.setFrom(envelopeFrom, instituicao.getNomeInstituicao());

        // Se o envelope-from for diferente do email da instituição, definir Reply-To
        if (instituicao.getEmailInstituicao() != null && !instituicao.getEmailInstituicao().isBlank()
                && !instituicao.getEmailInstituicao().equalsIgnoreCase(envelopeFrom)) {
            try {
                helper.setReplyTo(instituicao.getEmailInstituicao());
            } catch (Exception rtEx) {
                // Ignorar falhas no Reply-To, não é crítico
            }
        }

        mailSender.send(message);
    }

    /**
     * Registra log de postagem.
     */
    private void registrarLogPostagem(DisparoEmailBatch disparo, Pessoa pessoa, boolean sucesso, String erro) {
        try {
            LogPostagem log = new LogPostagem();
            log.setDataHoraPostagem(java.time.LocalDateTime.now());
            log.setOcorrenciaAtividadeId(null); // NULL para disparo genérico (não vinculado a atividade)
            log.setTituloAtividade("Disparo Email Genérico");
            log.setAssuntoDivulgacao(disparo.getAssunto());
            log.setAutorId(disparo.getUsuarioCriador() != null ? disparo.getUsuarioCriador().getId() : null);
            log.setQtEnviados(sucesso ? 1 : 0);
            log.setQtFalhas(sucesso ? 0 : 1);

            // Mensagem do log
            String mensagem = String.format("Tipo: %s, ID Disparo: %d, Destinatário: %s (%s), Status: %s",
                    disparo.getTipoDisparo(),
                    disparo.getId(),
                    pessoa.getNomePessoa(),
                    pessoa.getEmailPessoa(),
                    sucesso ? "ENVIADO" : "FALHA");

            if (!sucesso && erro != null) {
                mensagem += " - Erro: " + (erro.length() > 300 ? erro.substring(0, 300) + "..." : erro);
            }

            log.setMensagemLogPostagem(mensagem);
            logPostagemRepository.save(log);

        } catch (Exception e) {
            System.err.println("Erro ao registrar log de postagem: " + e.getMessage());
        }
    }

    /**
     * Busca disparo por ID.
     */
    public Optional<DisparoEmailBatch> buscarPorId(Long id) {
        return disparoBatchRepository.findById(id);
    }

    /**
     * Lista disparos por instituição.
     */
    public List<DisparoEmailBatch> listarPorInstituicao(Instituicao instituicao) {
        return disparoBatchRepository.findByInstituicaoOrderByDataCriacaoDesc(instituicao);
    }

    /**
     * Obtém disparo por ID (retorna objeto ou null).
     */
    public DisparoEmailBatch obterDisparo(Long id) {
        return disparoBatchRepository.findById(id).orElse(null);
    }

    /**
     * Lista disparos por instituição (sem Optional).
     */
    public List<DisparoEmailBatch> listarDisparosPorInstituicao(Instituicao instituicao) {
        return disparoBatchRepository.findByInstituicaoOrderByDataCriacaoDesc(instituicao);
    }

    /**
     * Cancela um disparo pendente ou em processamento.
     */
    @Transactional
    public void cancelarDisparo(Long disparoId) {
        DisparoEmailBatch disparo = disparoBatchRepository.findById(disparoId)
                .orElseThrow(() -> new RuntimeException("Disparo não encontrado: " + disparoId));

        if (disparo.getStatus() == StatusDisparo.PENDENTE || disparo.getStatus() == StatusDisparo.PROCESSANDO) {
            disparo.setStatus(StatusDisparo.CANCELADO);
            disparo.setDataFimProcessamento(LocalDateTime.now());
            disparoBatchRepository.save(disparo);
        } else {
            throw new RuntimeException("Disparo não pode ser cancelado. Status: " + disparo.getStatus());
        }
    }

    /**
     * Exclui um disparo de email do banco de dados.
     */
    @Transactional
    public void excluirDisparo(Long disparoId) {
        DisparoEmailBatch disparo = disparoBatchRepository.findById(disparoId)
                .orElseThrow(() -> new RuntimeException("Disparo não encontrado: " + disparoId));

        disparoBatchRepository.delete(disparo);
    }

    /**
     * Conta destinatários baseado nos filtros.
     */
    public Integer contarDestinatarios(DisparoEmailBatch disparo) {
        return listarDestinatarios(disparo).size();
    }

    /**
     * Carrega template HTML do classpath.
     */
    public String carregarTemplateHtml(String tipo) throws Exception {
        String templatePath = "/templates/emails/" + tipo + ".html";

        try (var inputStream = getClass().getResourceAsStream(templatePath)) {
            if (inputStream == null) {
                throw new Exception("Template não encontrado: " + templatePath);
            }

            String htmlCompleto = new String(inputStream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);

            // Extrair apenas o conteúdo do <body> para não afetar o CSS da página
            int bodyStart = htmlCompleto.indexOf("<body>");
            int bodyEnd = htmlCompleto.indexOf("</body>");

            if (bodyStart != -1 && bodyEnd != -1) {
                // Retorna apenas o conteúdo interno do body (sem as tags <body></body>)
                return htmlCompleto.substring(bodyStart + 6, bodyEnd).trim();
            }

            // Se não encontrar body tags, retorna o HTML completo (fallback)
            return htmlCompleto;
        }
    }
}
