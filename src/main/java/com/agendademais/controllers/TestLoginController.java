package com.agendademais.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.agendademais.entities.Instituicao;
import com.agendademais.entities.Usuario;
import com.agendademais.entities.UsuarioInstituicao;
import com.agendademais.repositories.InstituicaoRepository;
import com.agendademais.repositories.UsuarioInstituicaoRepository;
import com.agendademais.repositories.UsuarioRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class TestLoginController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private InstituicaoRepository instituicaoRepository;

    @Autowired
    private UsuarioInstituicaoRepository usuarioInstituicaoRepository;

    @GetMapping("/test-login")
    public String testLogin(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        try {
            // Buscar um usuário administrador para teste
            Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("admin1");
            if (usuarioOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("mensagemErro", "Usuário admin1 não encontrado");
                return "redirect:/acesso";
            }

            Usuario usuario = usuarioOpt.get();

            // Buscar uma instituição
            Optional<Instituicao> instituicaoOpt = instituicaoRepository.findAll().stream().findFirst();
            if (instituicaoOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("mensagemErro", "Nenhuma instituição encontrada");
                return "redirect:/acesso";
            }

            Instituicao instituicao = instituicaoOpt.get();

            // Buscar o vínculo usuário-instituição
            Optional<UsuarioInstituicao> usuarioInstOpt = usuarioInstituicaoRepository
                    .findByUsuarioIdAndInstituicaoId(usuario.getId(), instituicao.getId());
            if (usuarioInstOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("mensagemErro", "Vinculo usuario-instituição não encontrado");
                return "redirect:/acesso";
            }

            UsuarioInstituicao usuarioInst = usuarioInstOpt.get();

            // Configurar sessão
            session.setAttribute("usuarioLogado", usuario);
            session.setAttribute("instituicaoSelecionada", instituicao);
            session.setAttribute("nivelAcessoAtual", usuarioInst.getNivelAcessoUsuarioInstituicao());

            System.out.println("*** LOGIN AUTOMATICO REALIZADO ***");
            System.out.println("Usuario: " + usuario.getUsername());
            System.out.println("Instituição: " + instituicao.getNomeInstituicao());
            System.out.println("Nível: " + usuarioInst.getNivelAcessoUsuarioInstituicao());

            // Agora buscar os usuários para exibir diretamente
            List<UsuarioInstituicao> usuarios = usuarioInstituicaoRepository
                    .findByInstituicaoOrderByNivelAcessoUsuarioInstituicaoAsc(instituicao);

            // Preparar modelo
            model.addAttribute("usuarios", usuarios);
            model.addAttribute("usuarioLogado", usuario);
            model.addAttribute("instituicaoSelecionada", instituicao);
            model.addAttribute("nivelAcessoAtual", usuarioInst.getNivelAcessoUsuarioInstituicao());

            // Retornar template direto para não perder sessão
            return "gestao-usuarios/lista-usuarios";

        } catch (Exception e) {
            System.out.println("ERRO NO LOGIN AUTOMATICO: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro no login automático: " + e.getMessage());
            return "redirect:/acesso";
        }
    }
}
