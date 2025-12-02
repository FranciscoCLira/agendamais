package com.agendademais.controllers;

import com.agendademais.entities.ConfiguracaoSmtpGlobal;
import com.agendademais.entities.Usuario;
import com.agendademais.entities.UsuarioInstituicao;
import com.agendademais.repositories.ConfiguracaoSmtpGlobalRepository;
import com.agendademais.repositories.UsuarioInstituicaoRepository;
import com.agendademais.services.CryptoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Controller para gerenciamento de configurações SMTP globais
 * Acesso restrito a usuários de nível 0 (Controle Total)
 */
@Controller
@RequestMapping("/controle-total/configuracao-smtp-global")
public class ConfiguracaoSmtpGlobalController {

    @Autowired
    private ConfiguracaoSmtpGlobalRepository configuracaoSmtpGlobalRepository;

    @Autowired
    private UsuarioInstituicaoRepository usuarioInstituicaoRepository;

    @Autowired
    private CryptoService cryptoService;

    /**
     * Exibe o formulário de configuração SMTP global
     */
    @GetMapping
    public String configurar(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        // Validação de acesso: apenas nível 0 (Controle Total)
        Object usuarioLogado = session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Sessão expirada. Faça login novamente.");
            return "redirect:/acesso";
        }

        Usuario usuario = (Usuario) usuarioLogado;
        Long usuarioLogadoId = usuario.getId();

        // Verifica se o usuário tem nível 0 (Controle Total) ou nível 9 (Superusuário) em alguma instituição
        List<UsuarioInstituicao> vinculos = usuarioInstituicaoRepository.findByUsuarioId(usuarioLogadoId);
        boolean temAcesso = vinculos.stream()
                .anyMatch(v -> v.getNivelAcessoUsuarioInstituicao() == 0 || v.getNivelAcessoUsuarioInstituicao() == 9);

        if (!temAcesso) {
            redirectAttributes.addFlashAttribute("mensagemErro", 
                    "Acesso negado. Esta funcionalidade é restrita ao Controle Total (nível 0) ou Superusuário (nível 9).");
            return "redirect:/controle-total";
        }

        // Busca configuração ativa ou cria uma nova
        ConfiguracaoSmtpGlobal config = configuracaoSmtpGlobalRepository
                .findFirstByAtivoTrueOrderByDataCriacaoDesc()
                .orElse(new ConfiguracaoSmtpGlobal());

        model.addAttribute("configuracao", config);
        model.addAttribute("usuarioLogado", usuarioLogado);
        return "controle-total/configuracao-smtp-global";
    }

    /**
     * Salva a configuração SMTP global
     */
    @PostMapping("/salvar")
    public String salvar(@ModelAttribute ConfiguracaoSmtpGlobal configuracao,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        // Validação de acesso
        Object usuarioLogado = session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Sessão expirada. Faça login novamente.");
            return "redirect:/acesso";
        }

        Usuario usuario = (Usuario) usuarioLogado;
        Long usuarioLogadoId = usuario.getId();

        List<UsuarioInstituicao> vinculos = usuarioInstituicaoRepository.findByUsuarioId(usuarioLogadoId);
        boolean temAcesso = vinculos.stream()
                .anyMatch(v -> v.getNivelAcessoUsuarioInstituicao() == 0 || v.getNivelAcessoUsuarioInstituicao() == 9);

        if (!temAcesso) {
            redirectAttributes.addFlashAttribute("error", "Acesso negado. Esta funcionalidade é restrita ao Controle Total (nível 0) ou Superusuário (nível 9).");
            return "redirect:/controle-total";
        }

        try {
            // Se editando configuração existente, preserva senha se campo estiver vazio
            if (configuracao.getId() != null) {
                ConfiguracaoSmtpGlobal existente = configuracaoSmtpGlobalRepository
                        .findById(configuracao.getId())
                        .orElse(null);
                
                if (existente != null 
                        && (configuracao.getSmtpPassword() == null 
                        || configuracao.getSmtpPassword().trim().isEmpty())) {
                    // Mantém senha existente
                    configuracao.setSmtpPassword(existente.getSmtpPassword());
                } else if (configuracao.getSmtpPassword() != null 
                        && !configuracao.getSmtpPassword().startsWith("ENC(")) {
                    // Criptografa nova senha
                    String senhaCriptografada = cryptoService.encryptIfNeeded(configuracao.getSmtpPassword());
                    configuracao.setSmtpPassword(senhaCriptografada);
                }
            } else {
                // Nova configuração: criptografa senha
                if (configuracao.getSmtpPassword() != null 
                        && !configuracao.getSmtpPassword().startsWith("ENC(")) {
                    String senhaCriptografada = cryptoService.encryptIfNeeded(configuracao.getSmtpPassword());
                    configuracao.setSmtpPassword(senhaCriptografada);
                }
                configuracao.setDataCriacao(LocalDateTime.now());
            }

            // Desativa outras configurações antes de salvar
            if (configuracao.getAtivo()) {
                configuracaoSmtpGlobalRepository.findAll().forEach(c -> {
                    if (!c.getId().equals(configuracao.getId())) {
                        c.setAtivo(false);
                        configuracaoSmtpGlobalRepository.save(c);
                    }
                });
            }

            configuracao.setDataAtualizacao(LocalDateTime.now());
            configuracao.setUsuarioAtualizacao(usuario);
            configuracaoSmtpGlobalRepository.save(configuracao);

            redirectAttributes.addFlashAttribute("success", "Configuração SMTP global salva com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                    "Erro ao salvar configuração: " + e.getMessage());
        }

        return "redirect:/controle-total/configuracao-smtp-global";
    }

    /**
     * Busca senha SMTP atual do banco
     */
    @GetMapping("/senha-atual")
    @ResponseBody
    public Map<String, Object> getSenhaAtual(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        Object usuarioLogado = session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            response.put("success", false);
            response.put("senha", null);
            return response;
        }

        ConfiguracaoSmtpGlobal config = configuracaoSmtpGlobalRepository
                .findFirstByAtivoTrueOrderByDataCriacaoDesc()
                .orElse(null);

        if (config != null && config.getSmtpPassword() != null) {
            response.put("success", true);
            response.put("senha", config.getSmtpPassword());
        } else {
            response.put("success", false);
            response.put("senha", null);
        }
        
        return response;
    }

    /**
     * Testa a conexão SMTP
     */
    @PostMapping("/testar")
    @ResponseBody
    public Map<String, Object> testarConexao(@RequestParam String smtpHost,
                                             @RequestParam Integer smtpPort,
                                             @RequestParam String smtpUsername,
                                             @RequestParam String smtpPassword,
                                             HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        // Validação de acesso
        Object usuarioLogado = session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            response.put("success", false);
            response.put("message", "Sessão expirada");
            return response;
        }

        Usuario usuario = (Usuario) usuarioLogado;
        List<UsuarioInstituicao> vinculos = usuarioInstituicaoRepository.findByUsuarioId(usuario.getId());
        boolean temAcesso = vinculos.stream()
                .anyMatch(v -> v.getNivelAcessoUsuarioInstituicao() == 0 || v.getNivelAcessoUsuarioInstituicao() == 9);

        if (!temAcesso) {
            response.put("success", false);
            response.put("message", "Acesso negado");
            return response;
        }

        try {
            // Descriptografa senha se necessário
            String senhaReal = cryptoService.decryptIfNeeded(smtpPassword);
            
            System.err.println("[SMTP TEST] Host: " + smtpHost);
            System.err.println("[SMTP TEST] Port: " + smtpPort);
            System.err.println("[SMTP TEST] Username: " + smtpUsername);
            System.err.println("[SMTP TEST] Password received length: " + (smtpPassword != null ? smtpPassword.length() : "NULL"));
            System.err.println("[SMTP TEST] Password decrypted length: " + (senhaReal != null ? senhaReal.length() : "NULL"));
            System.err.println("[SMTP TEST] Password starts with 'ENC(': " + (smtpPassword != null && smtpPassword.startsWith("ENC(")));

            // Configura JavaMailSender
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost(smtpHost);
            mailSender.setPort(smtpPort);
            mailSender.setUsername(smtpUsername);
            mailSender.setPassword(senhaReal);

            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.ssl.trust", smtpHost);
            props.put("mail.smtp.timeout", "10000");
            props.put("mail.smtp.connectiontimeout", "10000");

            // Testa conexão
            mailSender.testConnection();

            response.put("success", true);
            response.put("message", "Conexão SMTP estabelecida com sucesso!");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erro ao conectar: " + e.getMessage());
            return response;
        }
    }
}
