package com.agendademais.controllers.instituicao;

import com.agendademais.entities.Instituicao;
import com.agendademais.services.InstituicaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/administrador/instituicao")
public class AdministradorInstituicaoController {
    @Autowired
    private InstituicaoService instituicaoService;

    // Exibe o formulário para editar email e situação da instituição do admin
    // logado
    @GetMapping("/editar")
    public String editarInstituicao(Model model, HttpSession session) {
        Object instituicaoSelecionada = session.getAttribute("instituicaoSelecionada");
        if (instituicaoSelecionada == null) {
            return "redirect:/acesso?mensagem=Session expired. Please log in again.";
        }
        
        Instituicao inst = (Instituicao) instituicaoSelecionada;
        
        model.addAttribute("instituicao", inst);
        return "instituicao/editar-instituicao-admin";
    }

    // Salva apenas email e situação, se permitido
    @PostMapping("/editar")
    public String salvarEdicao(@ModelAttribute Instituicao instituicao, HttpSession session,
            RedirectAttributes redirectAttributes) {
        Object instituicaoSelecionada = session.getAttribute("instituicaoSelecionada");
        if (instituicaoSelecionada == null) {
            redirectAttributes.addFlashAttribute("error", "Sessão expirada. Faça login novamente.");
            return "redirect:/acesso";
        }
        Instituicao inst = (Instituicao) instituicaoSelecionada;
        if ("I".equals(inst.getSituacaoInstituicao())) {
            redirectAttributes.addFlashAttribute("error", "Instituição inativa não pode ser editada.");
            return "redirect:/administrador/instituicao/editar";
        }
        // Update only allowed fields
        inst.setEmailInstituicao(instituicao.getEmailInstituicao());
        inst.setSituacaoInstituicao(instituicao.getSituacaoInstituicao());
        inst.setModoEnvioEmail(instituicao.getModoEnvioEmail());
        
        // Update SMTP configuration fields
        // Converte string vazia em NULL para campos SMTP
        inst.setSmtpHost(instituicao.getSmtpHost() != null && !instituicao.getSmtpHost().trim().isEmpty() 
                ? instituicao.getSmtpHost() : null);
        inst.setSmtpPort(instituicao.getSmtpPort());
        inst.setSmtpUsername(instituicao.getSmtpUsername() != null && !instituicao.getSmtpUsername().trim().isEmpty() 
                ? instituicao.getSmtpUsername() : null);
        
        // Only update password if not blank (empty means "keep existing")
        if (instituicao.getSmtpPassword() != null && !instituicao.getSmtpPassword().trim().isEmpty()) {
            inst.setSmtpPassword(instituicao.getSmtpPassword());
        }
        inst.setSmtpSsl(instituicao.getSmtpSsl());
        inst.setDataUltimaAtualizacao(java.time.LocalDate.now());
        // Save using the service/repository
        instituicaoService.save(inst);
        // Update session
        session.setAttribute("instituicaoSelecionada", inst);
        redirectAttributes.addFlashAttribute("success", "Dados da instituição atualizados com sucesso.");
        return "redirect:/administrador/instituicao/editar";
    }
}
