package com.agendademais.controllers;

import com.agendademais.entities.Instituicao;
import com.agendademais.services.InstituicaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/instituicao")
public class InstituicaoSmtpController {

    @Autowired
    private InstituicaoService instituicaoService;

    @Autowired
    private com.agendademais.services.CryptoService cryptoService;

    @GetMapping("/smtp/{id}")
    public String editSmtp(@PathVariable Long id, Model model) {
        Instituicao inst = instituicaoService.findById(id).orElse(null);
        model.addAttribute("instituicao", inst);
        return "instituicao/smtp_edit";
    }

    @PostMapping("/smtp/{id}")
    public String saveSmtp(@PathVariable Long id, @ModelAttribute Instituicao instituicao) {
        // ensure the ID is consistent
        instituicao.setId(id);
        instituicaoService.save(instituicao);
        return "redirect:/admin/instituicao/smtp/" + id + "?saved=1";
    }

    @PostMapping("/smtp/{id}/test")
    @ResponseBody
    public String testSmtp(@PathVariable Long id, @ModelAttribute Instituicao instituicao) {
        // Build a temporary JavaMailSenderImpl and try to connect
        try {
            org.springframework.mail.javamail.JavaMailSenderImpl sender = new org.springframework.mail.javamail.JavaMailSenderImpl();
            sender.setHost(instituicao.getSmtpHost());
            if (instituicao.getSmtpPort() != null)
                sender.setPort(instituicao.getSmtpPort());
            sender.setUsername(instituicao.getSmtpUsername());
            String pwd = instituicao.getSmtpPassword();
            String decrypted = cryptoService.decryptIfNeeded(pwd);
            sender.setPassword(decrypted);
            java.util.Properties props = sender.getJavaMailProperties();
            props.put("mail.smtp.auth", "true");
            if (Boolean.TRUE.equals(instituicao.getSmtpSsl()))
                props.put("mail.smtp.ssl.enable", "true");
            else
                props.put("mail.smtp.starttls.enable", "true");
            jakarta.mail.Session session = sender.getSession();
            jakarta.mail.Transport transport = session.getTransport("smtp");
            try {
                transport.connect(sender.getHost(), sender.getPort(), sender.getUsername(), sender.getPassword());
                transport.close();
                return "OK";
            } catch (Exception ex) {
                return "FAIL: " + ex.getMessage();
            }
        } catch (Exception e) {
            return "FAIL: " + e.getMessage();
        }
    }
}
