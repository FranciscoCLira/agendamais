package com.agendademais.controllers;

import com.agendademais.entities.Usuario;
import com.agendademais.entities.UsuarioInstituicao;
import com.agendademais.repositories.UsuarioRepository;
import com.agendademais.repositories.UsuarioInstituicaoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioInstituicaoRepository usuarioInstituicaoRepository;

        
    @GetMapping
    public String listarUsuarios(HttpSession session, Model model) {
        List<Usuario> usuarios = usuarioRepository.findAll();
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("nomeInstituicao", session.getAttribute("nomeInstituicao"));
        return "usuario-lista";
    }

    @PostMapping("/alterar-nivel")
    public String alterarNivel(@RequestParam Long id, @RequestParam int novoNivel, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        Usuario alvo = usuarioRepository.findById(id).orElse(null);

        if (alvo == null || usuarioLogado == null) return "redirect:/usuarios";

        boolean permissao = usuarioLogado.getNivelAcessoUsuario() == 9 ||
                (usuarioLogado.getNivelAcessoUsuario() == 5 && novoNivel < 5);

        if (permissao) {
            alvo.setNivelAcessoUsuario(novoNivel);
            usuarioRepository.save(alvo);
        }
        return "redirect:/usuarios";
    }

    @PostMapping("/remover")
    public String removerUsuario(@RequestParam Long id, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        Usuario alvo = usuarioRepository.findById(id).orElse(null);

        if (alvo == null || usuarioLogado == null) return "redirect:/usuarios";

        boolean permissao = usuarioLogado.getNivelAcessoUsuario() == 9 ||
                (usuarioLogado.getNivelAcessoUsuario() == 5 && alvo.getNivelAcessoUsuario() < 5);

        if (permissao) {
            usuarioRepository.delete(alvo);
        }
        return "redirect:/usuarios";
    }

    @PostMapping("/situacao")
    public String alterarSituacaoUsuarioInstituicao(@RequestParam Long idUsuario, @RequestParam String novaSituacao, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) return "redirect:/login";

        List<UsuarioInstituicao> acessos = usuarioInstituicaoRepository.findByUsuarioId(idUsuario);

        for (UsuarioInstituicao acesso : acessos) {
            boolean podeAlterar = usuarioLogado.getNivelAcessoUsuario() == 9 ||
                    (usuarioLogado.getNivelAcessoUsuario() == 5 &&
                     usuarioInstituicaoRepository.existsByUsuarioIdAndInstituicaoId(
                         usuarioLogado.getId(), acesso.getInstituicao().getId()));

            if (podeAlterar) {
                acesso.setSitAcessoUsuarioInstituicao(novaSituacao);
                usuarioInstituicaoRepository.save(acesso);
            }
        }

        return "redirect:/usuarios";
    }
}
