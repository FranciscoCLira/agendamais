package com.agendademais.controllers;

import com.agendademais.entities.FuncaoAutorCustomizada;
import com.agendademais.entities.Usuario;
import com.agendademais.repositories.FuncaoAutorCustomizadaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

/**
 * Controller para gerenciar funções personalizadas de autor
 * Disponível para usuários com nível 0 (controle-total)
 */
@Controller
@RequestMapping("/gestao/funcoes-autor")
public class FuncaoAutorController {

    @Autowired
    private FuncaoAutorCustomizadaRepository funcaoRepository;

    /**
     * Lista todas as funções personalizadas
     */
    @GetMapping
    public String listarFuncoes(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        Integer nivelAcesso = (Integer) session.getAttribute("nivelAcessoAtual");

        // Verificar permissões
        if (usuario == null) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Sessão inválida. Faça login novamente.");
            return "redirect:/acesso";
        }

        if (nivelAcesso == null || nivelAcesso != 0) {
            redirectAttributes.addFlashAttribute("mensagemErro", 
                "Acesso negado. Funcionalidade disponível apenas para controle total.");
            return "redirect:/participante";
        }

        List<FuncaoAutorCustomizada> funcoes = funcaoRepository.findAll();
        model.addAttribute("funcoes", funcoes);
        model.addAttribute("novaFuncao", new FuncaoAutorCustomizada());

        return "gestao/funcoes-autor";
    }

    /**
     * Criar nova função personalizada
     */
    @PostMapping("/criar")
    public String criarFuncao(@ModelAttribute FuncaoAutorCustomizada funcao,
                              HttpSession session, 
                              RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        Integer nivelAcesso = (Integer) session.getAttribute("nivelAcessoAtual");

        // Verificar permissões
        if (usuario == null || nivelAcesso == null || nivelAcesso != 0) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Acesso negado.");
            return "redirect:/acesso";
        }

        // Verificar se já existe
        if (funcaoRepository.existsByNomeFuncao(funcao.getNomeFuncao())) {
            redirectAttributes.addFlashAttribute("mensagemErro", 
                "Já existe uma função com este nome.");
            return "redirect:/gestao/funcoes-autor";
        }

        funcao.setCriadoPor(usuario);
        funcaoRepository.save(funcao);

        redirectAttributes.addFlashAttribute("mensagemSucesso", 
            "Função '" + funcao.getNomeFuncao() + "' criada com sucesso!");

        return "redirect:/gestao/funcoes-autor";
    }

    /**
     * Ativar/Desativar função
     */
    @PostMapping("/toggle/{id}")
    public String toggleFuncao(@PathVariable Long id,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        Integer nivelAcesso = (Integer) session.getAttribute("nivelAcessoAtual");

        // Verificar permissões
        if (usuario == null || nivelAcesso == null || nivelAcesso != 0) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Acesso negado.");
            return "redirect:/acesso";
        }

        Optional<FuncaoAutorCustomizada> funcaoOpt = funcaoRepository.findById(id);
        if (funcaoOpt.isPresent()) {
            FuncaoAutorCustomizada funcao = funcaoOpt.get();
            funcao.setAtiva(!funcao.isAtiva());
            funcaoRepository.save(funcao);

            String status = funcao.isAtiva() ? "ativada" : "desativada";
            redirectAttributes.addFlashAttribute("mensagemSucesso", 
                "Função '" + funcao.getNomeFuncao() + "' " + status + " com sucesso!");
        } else {
            redirectAttributes.addFlashAttribute("mensagemErro", "Função não encontrada.");
        }

        return "redirect:/gestao/funcoes-autor";
    }

    /**
     * Excluir função
     */
    @PostMapping("/excluir/{id}")
    public String excluirFuncao(@PathVariable Long id,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        Integer nivelAcesso = (Integer) session.getAttribute("nivelAcessoAtual");

        // Verificar permissões
        if (usuario == null || nivelAcesso == null || nivelAcesso != 0) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Acesso negado.");
            return "redirect:/acesso";
        }

        Optional<FuncaoAutorCustomizada> funcaoOpt = funcaoRepository.findById(id);
        if (funcaoOpt.isPresent()) {
            FuncaoAutorCustomizada funcao = funcaoOpt.get();
            String nomeFuncao = funcao.getNomeFuncao();
            funcaoRepository.delete(funcao);

            redirectAttributes.addFlashAttribute("mensagemSucesso", 
                "Função '" + nomeFuncao + "' excluída com sucesso!");
        } else {
            redirectAttributes.addFlashAttribute("mensagemErro", "Função não encontrada.");
        }

        return "redirect:/gestao/funcoes-autor";
    }
}
