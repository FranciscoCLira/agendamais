package com.agendademais.controllers;

import com.agendademais.entities.Instituicao;
import com.agendademais.services.InstituicaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/gerenciar-instituicoes")
public class ControleTotalInstituicaoController {
    @Autowired
    private InstituicaoService instituicaoService;

    @GetMapping
    public String listarInstituicoes(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        Object usuarioLogado = session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Sessão expirada. Faça login novamente.");
            return "redirect:/acesso";
        }
        List<Instituicao> instituicoes = instituicaoService.findAll();
        model.addAttribute("instituicoes", instituicoes);
        model.addAttribute("usuarioLogado", usuarioLogado);
        return "controle-total/listar-instituicoes";
    }

    @GetMapping("/nova")
    public String novaInstituicaoForm(Model model) {
        model.addAttribute("instituicao", new Instituicao());
        return "controle-total/editar-instituicao";
    }

    @PostMapping("/salvar")
    public String salvarInstituicao(@ModelAttribute Instituicao instituicao, RedirectAttributes redirectAttributes) {
        // If editing existing institution, preserve password if field is blank
        if (instituicao.getId() != null) {
            Instituicao existing = instituicaoService.findById(instituicao.getId()).orElse(null);
            if (existing != null && (instituicao.getSmtpPassword() == null || instituicao.getSmtpPassword().trim().isEmpty())) {
                // Keep existing password if field is blank
                instituicao.setSmtpPassword(existing.getSmtpPassword());
            }
        }
        instituicao.setDataUltimaAtualizacao(java.time.LocalDate.now());
        instituicaoService.save(instituicao);
        redirectAttributes.addFlashAttribute("success", "Instituição salva com sucesso.");
        return "redirect:/gerenciar-instituicoes";
    }

    @GetMapping("/editar/{id}")
    public String editarInstituicao(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Instituicao inst = instituicaoService.findById(id).orElse(null);
        if (inst == null) {
            redirectAttributes.addFlashAttribute("error", "Instituição não encontrada.");
            return "redirect:/gerenciar-instituicoes";
        }
        model.addAttribute("instituicao", inst);
        return "controle-total/editar-instituicao";
    }

    @PostMapping("/excluir/{id}")
    public String excluirInstituicao(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean deleted = instituicaoService.deleteIfNoRelations(id);
        if (deleted) {
            redirectAttributes.addFlashAttribute("success", "Instituição excluída com sucesso.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Não é possível excluir: existem vínculos relacionados.");
        }
        return "redirect:/gerenciar-instituicoes";
    }
}
